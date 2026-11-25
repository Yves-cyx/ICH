#!/usr/bin/env python3
"""Exercise the deployed service with real HTTP requests and isolated accounts."""
import json
import os
import urllib.error
import urllib.parse
import urllib.request
import uuid

base = os.environ.get("API_BASE", "http://127.0.0.1:8096").rstrip("/")


def request_api(method, path, body=None, token=None, status=200):
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
        return result, reply.headers


def call(method, path, body=None, token=None, status=200):
    return request_api(method, path, body, token, status)[0]


def query(path, **parameters):
    return path + "?" + urllib.parse.urlencode(parameters)


def ids(entries):
    return [entry["id"] for entry in entries]


def register():
    account = {"username": "test_" + uuid.uuid4().hex[:10], "password": "TestPass456!"}
    token = call("POST", "/api/auth/register", account, status=201)["data"]["token"]
    assert call("POST", "/api/auth/login", account)["data"]["token"]
    return token


api_paths = call("GET", "/v3/api-docs")["paths"]
assert "/api/works/{id}" in api_paths
for endpoint in ("/api/heritage", "/api/me/favorites"):
    assert "X-Total-Count" in api_paths[endpoint]["get"]["responses"]["200"]["headers"]
assert {"put", "delete"} <= api_paths["/api/me/favorites/{heritageId}"].keys()
entries = call("GET", "/api/heritage")["data"]
assert len(entries) >= 6
assert call("GET", "/api/heritage/" + str(entries[0]["id"]))["data"]["description"]

# 搜索覆盖三个字段，分页须保留原有数组结构。
for keyword in ("  皮影戏  ", "华县", "一盏灯"):
    result = call("GET", query("/api/heritage", keyword=keyword, category="表演"))
    assert ids(result["data"]) == [1], (keyword, result)
assert call("GET", query("/api/heritage", keyword="华县", category="工艺"))["data"] == []
for wildcard in ("%", "_", "!"):
    expected = [item["id"] for item in entries if any(wildcard in item[key] for key in ("title", "region", "summary"))]
    assert ids(call("GET", query("/api/heritage", keyword=wildcard))["data"]) == expected
assert ids(call("GET", query("/api/heritage", keyword="  "))["data"]) == ids(entries)
page, headers = request_api("GET", query("/api/heritage", category="工艺", page=2, size=1))
crafts = [entry["id"] for entry in entries if entry["category"] == "工艺"]
assert ids(page["data"]) == crafts[1:2]
assert int(headers["X-Total-Count"]) == len(crafts)
page, headers = request_api("GET", query("/api/heritage", size=1))
assert ids(page["data"]) == ids(entries)[:1] and int(headers["X-Total-Count"]) == len(entries)
page, headers = request_api("GET", query("/api/heritage", page=999999))
assert page["data"] == [] and int(headers["X-Total-Count"]) == len(entries)
_, headers = request_api("GET", "/api/heritage")
assert "X-Total-Count" not in headers
for invalid in ({"page": 0}, {"size": 51}, {"page": ""}, {"size": ""}, {"keyword": "字" * 81}, {"category": "其他"}):
    assert call("GET", query("/api/heritage", **invalid), status=400)["code"] == 400

call("GET", "/api/me", status=401)
assert call("GET", "/api/me", token="invalid", status=401)["code"] == 401
token = register()
other = register()

favorites = "/api/me/favorites"
first, second = favorites + "/1", favorites + "/2"
call("GET", favorites, status=401)
call("PUT", first, status=401)
call("DELETE", first, status=401)
assert call("GET", favorites, token=token)["data"] == []
call("PUT", favorites + "/9223372036854775807", token=token, status=404)
call("PUT", favorites + "/0", token=token, status=400)
call("GET", query(favorites, page=0), token=token, status=400)
call("GET", query(favorites, size=""), token=token, status=400)
assert call("PUT", first, token=token)["data"] is None
call("PUT", second, token=token)
call("PUT", first, token=token)
assert ids(call("GET", favorites, token=token)["data"]) == [2, 1]
page, headers = request_api("GET", query(favorites, page=2, size=1), token=token)
assert ids(page["data"]) == [1] and int(headers["X-Total-Count"]) == 2
assert call("GET", favorites, token=other)["data"] == []
call("PUT", first, token=other)
assert call("DELETE", first, token=token)["data"] is None
call("DELETE", first, token=token)
assert ids(call("GET", favorites, token=token)["data"]) == [2]
assert ids(call("GET", favorites, token=other)["data"]) == [1]

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
print("PASS: catalog search and paging, favorites, authentication, profile, works, account isolation")
