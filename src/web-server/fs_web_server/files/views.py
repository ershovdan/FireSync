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


def getFile(request):
    try:
        req = request.GET["req"]
        id = request.GET["id"]
        key = request.GET["key"]

        with open(db_cfg_path, "r") as file:
            db_cfg_json = json.loads(file.read())

            conn = psycopg2.connect(database=db_cfg_json["name"],
                                    host=db_cfg_json["host"],
                                    user=db_cfg_json["user"],
                                    password=db_cfg_json["password"],
                                    port=db_cfg_json["port"])
        cur = conn.cursor()

        cur.execute('SELECT * FROM "List" WHERE id=' + id + "AND key='" + key + "';")

        length = 0
        for i in cur.fetchall():
            length += 1

        conn.commit()
        conn.close()

        if length > 0:
            if req == "file_list":
                return JsonResponse({"answer": "/static/buffer/zipped/" + key + "op" + id + ".json"})
            elif req == "full":
                return JsonResponse({"answer": ["/static/buffer/zipped/" + key + "op" + id + ".json", "/static/buffer/zipped/" + key + "op" + id]})
            elif req == "file":
                path = request.GET["path"]

                with open(db_cfg_path, "r") as file:
                    db_cfg_json = json.loads(file.read())

                    conn = psycopg2.connect(database=db_cfg_json["name"],
                                            host=db_cfg_json["host"],
                                            user=db_cfg_json["user"],
                                            password=db_cfg_json["password"],
                                            port=db_cfg_json["port"])


                k = ""
                for i in range(32):
                    k += random.choice(string.ascii_letters)


                cur = conn.cursor()
                cur.execute("INSERT INTO \"Files\" (id, path, status, out_key) VALUES (" + id + ", '" + path + "', 1, '" + k + "');")
                conn.commit()
                conn.close()

                return JsonResponse({"answer": k})
        else:
            raise Exception

        return JsonResponse({"answer": "false"})
    except:
        return JsonResponse({"answer": "false"})