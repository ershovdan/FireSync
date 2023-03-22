import json

from django.http import JsonResponse
from django.shortcuts import render, redirect
import pathlib
import sqlite3
import os
import psycopg2
import datetime


module_dir = pathlib.Path(os.path.dirname(__file__))
db_cfg_path = os.path.join(module_dir.parent.parent.parent, 'cfg', 'db.cfg')


right_menu_path = os.path.join(module_dir.parent.parent.parent, 'db', 'right_menu')


def home(request):
    return render(request, 'interface/home.html')


def add(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        cur = conn.cursor()
    except Exception:
        pass

    with open(os.path.join(right_menu_path, "total_shares.txt"), "r") as file:
        total_shares = file.read()

    context = {"total_shares": total_shares}

    if request.method == "POST":
        cur.execute("INSERT INTO \"List\" (name, path, status) VALUES ('" + request.POST["name"] + "', '" + request.POST['path'] + "', 5);")

        print(request.POST)

        conn.commit()
        conn.close()
        return redirect('/add', context)
    conn.commit()
    conn.close()
    return render(request, 'interface/add.html', context)


def list(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        cur = conn.cursor()
    except Exception:
        pass

    if len(request.GET) == 0:
        cur.execute('SELECT * FROM "List" WHERE status > 0;')

        listOfShares = []
        for i in cur.fetchall():
            listOfShares.append({"id": i[0], "path": i[2], "name": i[1], "key": i[4]})

        context = {"list": listOfShares}
        conn.commit()
        conn.close()
        return render(request, 'interface/list.html', context)


def more_info(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        cur = conn.cursor()
    except Exception:
        pass


    cur.execute('SELECT * FROM "List" WHERE id = ' + request.GET["id"] + ';')

    context = {}

    for i in cur.fetchall():
        context["main"] = {"id": i[0], "path": i[2], "status": i[3], "name": i[1], "key": i[4]}


    if request.method == "POST":
        cur.execute('UPDATE "List" SET name = ' + "'" + request.POST["new_name"] + "'" + "WHERE id = " + str(i[0]) + ";")


    conn.commit()
    conn.close()

    return render(request, 'interface/more_info.html', context)


def getData(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        cur = conn.cursor()
    except Exception:
        pass

    if request.GET["check"] == "total_shares":
        cur.execute('SELECT * FROM "List" WHERE status > 0;')

        count = 0
        for i in cur.fetchall():
            count += 1

        conn.commit()
        conn.close()
        return JsonResponse({"answer": str(count)})
    elif request.GET["check"] == "connected_users_5":
        cur.execute('SELECT * FROM "Connected" ORDER BY "time" DESC LIMIT 30;')

        time = []
        users = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            users.append(i[1])

        time.reverse()
        users.reverse()

        conn.commit()
        conn.close()
        return JsonResponse({"time": time, "users": users})
    elif request.GET["check"] == "connected_users_30":
        cur.execute('SELECT * FROM "Connected" ORDER BY "time" DESC LIMIT 180;')

        time = []
        users = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            users.append(i[1])

        time.reverse()
        users.reverse()

        conn.commit()
        conn.close()
        return JsonResponse({"time": time, "users": users})
    elif request.GET["check"] == "connected_users_60":
        cur.execute('SELECT * FROM "Connected" ORDER BY "time" DESC LIMIT 360;')

        time = []
        users = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            users.append(i[1])

        time.reverse()
        users.reverse()

        conn.commit()
        conn.close()
        return JsonResponse({"time": time, "users": users})

    cur.execute('SELECT * FROM "List" WHERE id=' + request.GET["id"] + "AND key='" + request.GET["key"] +  "';")

    length = 0
    for i in cur.fetchall():
        length += 1

    if length > 0:
        if request.GET["check"] == "status":
            cur.execute('SELECT status FROM "List" WHERE id=' + request.GET["id"] + "AND key='" + request.GET["key"] + "';")

            status = 0
            for i in cur.fetchall():
                status = i[0]

            conn.commit()
            conn.close()
            return JsonResponse({"answer": str(status)})


    conn.commit()
    conn.close()
    return JsonResponse({"answer": "null"})

def preferences(request):
    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    print(db_cfg_json)

    db_status = "true"

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])
        conn.close()
    except Exception:
        db_status = "false"

    context = {"db_data": db_cfg_json, "db_status": db_status}

    if request.method == "POST":
        changedDB = {}
        if request.POST["db_name"] != db_cfg_json["name"]:
            changedDB["name"] = request.POST["db_name"]
        if request.POST["db_host"] != db_cfg_json["host"]:
            changedDB["host"] = request.POST["db_host"]
        if request.POST["db_port"] != db_cfg_json["port"]:
            changedDB["port"] = request.POST["db_port"]
        if request.POST["db_user"] != db_cfg_json["user"]:
            changedDB["user"] = request.POST["db_user"]
        if request.POST["db_password"] != db_cfg_json["password"]:
            changedDB["password"] = request.POST["db_password"]

        for i in changedDB.keys():
            db_cfg_json[i] = changedDB[i]

        with open(db_cfg_path, "w") as file:
            file.write(json.dumps(db_cfg_json))


    return render(request, 'interface/preferences.html', context)