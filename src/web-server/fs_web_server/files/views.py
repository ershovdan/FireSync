import json
import os
import pathlib
import random
import string

import psycopg2
from django.http import JsonResponse
from django.shortcuts import render, redirect

module_dir = pathlib.Path(os.path.dirname(__file__))
data_path = os.path.join(pathlib.Path.home(), 'FireSyncData')
db_cfg_path = os.path.join(data_path, 'cfg', 'db.cfg')


def getJsonFile(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        cur = conn.cursor()

        id = request.GET["id"]
        key = request.GET["key"]

    except Exception:
        return JsonResponse({"answer": "false"})

    cur.execute('SELECT * FROM "List" WHERE id = ' + id + "AND key = '" + key + "';")

    length = 0
    for i in cur.fetchall():
        print(i)
        length += 1

    if length == 0:
        return JsonResponse({"answer": "false"})

    conn.commit()
    conn.close()
    return redirect("/static/buffer/zipped/" + str(key) + "op" + str(id) + ".json")


def getFile(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        cur = conn.cursor()

        id = request.GET["id"]
        key = request.GET["key"]
        file_id = request.GET["file_id"]

    except Exception:
        return JsonResponse({"answer": "false"})

    cur.execute('SELECT * FROM "List" WHERE id = ' + id + "AND key = '" + key + "';")

    length = 0
    for i in cur.fetchall():
        print(i)
        length += 1

    if length == 0:
        return JsonResponse({"answer": "false"})

    if not (os.path.exists(os.path.join(pathlib.Path.home(), 'FireSyncData', 'buffer', 'zipped', str(key) + "op" + str(id), file_id + ".zip"))):
        return JsonResponse({"answer": "false"})

    conn.commit()
    conn.close()
    return redirect("/static/buffer/zipped/" + str(key) + "op" + str(id) + "/" + file_id + ".zip")