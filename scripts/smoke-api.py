#!/usr/bin/env python3
"""Exercise the deployed service with real HTTP requests and isolated accounts."""
import json
import os
import urllib.error
import urllib.request
import uuid

base = os.environ.get("API_BASE", "http://127.0.0.1:8096").rstrip("/")


def call(method, path, body=None, token=None, status=200):
    headers = {}
    if body is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = "Bearer " + token
    request = urllib.request.Request(
        base + path, json.dumps(body).encode() if body is not None else None,
        headers, method=method
    )
    try:
        reply = urllib.request.urlopen(request, timeout=8)
    except urllib.error.HTTPError as error:
        reply = error
    with reply:
        result = json.load(reply)
        assert reply.status == status, (path, reply.status, result)
    return result


def register():
    account = {"username": "test_" + uuid.uuid4().hex[:10], "password": "TestPass456!"}
    token = call("POST", "/api/auth/register", account, status=201)["data"]["token"]
    assert call("POST", "/api/auth/login", account)["data"]["token"]
    return token


assert "/api/works/{id}" in call("GET", "/v3/api-docs")["paths"]
entries = call("GET", "/api/heritage")["data"]
assert len(entries) >= 6
assert call("GET", "/api/heritage/" + str(entries[0]["id"]))["data"]["description"]
call("GET", "/api/me", status=401)
assert call("GET", "/api/me", token="invalid", status=401)["code"] == 401
token = register()
other = register()
call("PUT", "/api/me", {"nickname": "皮影访客", "bio": "我的非遗笔记"}, token)
assert call("GET", "/api/me", token=token)["data"]["nickname"] == "皮影访客"
draft = {"name": "测试皮影", "shape": "scholar", "palette": "cinnabar", "x": 0.5, "y": 0.6, "angle": 15}
call("POST", "/api/works", {**draft, "x": 2}, token, 400)
work = call("POST", "/api/works", draft, token, 201)["data"]
path = "/api/works/" + str(work["id"])
assert len(call("GET", "/api/works", token=token)["data"]) == 1
assert call("GET", "/api/works", token=other)["data"] == []
call("PUT", path, draft, other, 404)
call("DELETE", path, token=other, status=404)
draft["name"] = "已编辑"
assert call("PUT", path, draft, token)["data"]["name"] == "已编辑"
call("DELETE", path, token=token)
assert call("GET", "/api/works", token=token)["data"] == []
print("PASS: OpenAPI, catalog, registration, login, profile, validation, works CRUD, account isolation")
