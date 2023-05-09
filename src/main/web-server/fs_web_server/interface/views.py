import json
from django.http import JsonResponse
from django.shortcuts import render, redirect
import os
import psycopg2
import pathlib


module_dir = pathlib.Path(os.path.dirname(__file__))
data_path = os.path.join(pathlib.Path.home(), 'FireSyncData')
db_cfg_path = os.path.join(data_path, 'cfg', 'db.cfg')
main_cfg_path = os.path.join(data_path, 'cfg', 'main.cfg')
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

    with open(os.path.join(data_path, 'db', 'right_menu', 'total_shares.txt'), "r") as file:
        total_shares = file.read()

    context = {"total_shares": total_shares}

    if request.method == "POST":
        cur.execute("INSERT INTO \"List\" (name, path, status) VALUES ('" + request.POST["name"] + "', '" + request.POST['path'] + "', 5);")

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
        print(db_cfg_json)
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
    try:
        request.GET["check"] == " "
    except:
        return JsonResponse({"answer": "false"})

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
        for j in range(5):
            conn = psycopg2.connect(database=db_cfg_json["name"],
                                    host=db_cfg_json["host"],
                                    user=db_cfg_json["user"],
                                    password=db_cfg_json["password"],
                                    port=db_cfg_json["port"])
            try:
                cur = conn.cursor()
                break
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
        share_id = request.GET["id"]

        cur.execute('SELECT * FROM "Connected" WHERE id = ' + share_id + " AND time > NOW() - INTERVAL '5 minutes';")

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
        share_id = request.GET["id"]

        cur.execute('SELECT * FROM "Connected" WHERE id = ' + share_id + " AND time > NOW() - INTERVAL '30 minutes';")

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
    elif request.GET["check"] == "network_5":
        cur.execute('SELECT * FROM "Network" ORDER BY "time" DESC LIMIT 30;')

        time = []
        net = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            net.append(i[1])

        time.reverse()
        net.reverse()

        conn.commit()
        conn.close()
        return JsonResponse({"time": time, "net": net})
    elif request.GET["check"] == "network_15":
        cur.execute('SELECT * FROM "Network" ORDER BY "time" DESC LIMIT 90;')

        time = []
        net = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            net.append(i[1])

        time.reverse()
        net.reverse()

        conn.commit()
        conn.close()
        return JsonResponse({"time": time, "net": net})
    elif request.GET["check"] == "network_30":
        cur.execute('SELECT * FROM "Network" ORDER BY "time" DESC LIMIT 180;')

        time = []
        net = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            net.append(i[1])

        time.reverse()
        net.reverse()

        time_final = []
        net_final = []

        for i in range(0, len(time), 2):
            time_final.append(time[i])
            net_final.append(net[i])

        conn.commit()
        conn.close()
        return JsonResponse({"time": time_final, "net": net_final})
    elif request.GET["check"] == "network_60":
        cur.execute('SELECT * FROM "Network" ORDER BY "time" DESC LIMIT 360;')

        time = []
        net = []

        for i in cur.fetchall():
            date = i[0]
            time.append(date.strftime("%H:%M:%S"))
            net.append(i[1])

        time.reverse()
        net.reverse()

        time_final = []
        net_final = []

        for i in range(0, len(time), 4):
            time_final.append(time[i])
            net_final.append(net[i])

        conn.commit()
        conn.close()
        return JsonResponse({"time": time_final, "net": net_final})
    elif request.GET["check"] == "connected_users_60":
        share_id = request.GET["id"]

        cur.execute('SELECT * FROM "Connected" WHERE id = ' + share_id + " AND time > NOW() - INTERVAL '60 minutes';")

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
    elif request.GET["check"] == "connected_users_all_5":
        cur.execute('SELECT * FROM "Connected" WHERE id = ' + "-1" + " AND time > NOW() - INTERVAL '5 minutes';")

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
    elif request.GET["check"] == "connected_users_all_30":
        cur.execute('SELECT * FROM "Connected" WHERE id = ' + "-1" + " AND time > NOW() - INTERVAL '30 minutes';")

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
    elif request.GET["check"] == "connected_users_all_60":
        cur.execute('SELECT * FROM "Connected" WHERE id = ' + "-1" + " AND time > NOW() - INTERVAL '60 minutes';")

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
    elif request.GET["check"] == "now_operating":
        cur.execute('SELECT value_str FROM "Other" WHERE type = ' + "'now_operating_total'" + ';')

        name = ""
        for i in cur.fetchall():
            name = i[0]

        conn.commit()
        conn.close()
        return JsonResponse({"answer": name})

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
    with open(main_cfg_path, "r") as file:
        main_cfg_json = json.loads(file.read())

    with open(db_cfg_path, "r") as file:
        db_cfg_json = json.loads(file.read())

    db_status = "true"

    net_interface = ""

    try:
        conn = psycopg2.connect(database=db_cfg_json["name"],
                                host=db_cfg_json["host"],
                                user=db_cfg_json["user"],
                                password=db_cfg_json["password"],
                                port=db_cfg_json["port"])

        cur = conn.cursor()

        cur.execute('SELECT value_str FROM "Other" WHERE type = ' + "'net_interface'" + ';')

        for i in cur.fetchall():
            net_interface = i[0]

        conn.commit()
        conn.close()
    except Exception:
        db_status = "false"

    main_cfg_json["net_interface"] = net_interface

    context = {"db_data": db_cfg_json, "db_status": db_status, "main_data": main_cfg_json}

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

        print(main_cfg_json)

        changedMain = {}
        if request.POST["webserver_port"] != main_cfg_json["web_server_port"]:
            changedMain["web_server_port"] = request.POST["webserver_port"]
        if request.POST["net_interface"] != main_cfg_json["net_interface"]:
            conn = psycopg2.connect(database=db_cfg_json["name"],
                                    host=db_cfg_json["host"],
                                    user=db_cfg_json["user"],
                                    password=db_cfg_json["password"],
                                    port=db_cfg_json["port"])

            cur = conn.cursor()

            cur.execute('UPDATE "Other" SET value_str = ' + "'" + request.POST["net_interface"] + "'" + " WHERE type='net_interface';")

            conn.commit()
            conn.close()

        for i in changedDB.keys():
            db_cfg_json[i] = changedDB[i]

        for i in changedMain.keys():
            main_cfg_json[i] = changedMain[i]

        with open(db_cfg_path, "w") as file:
            file.write(json.dumps(db_cfg_json))

        with open(main_cfg_path, "w") as file:
            file.write(json.dumps(main_cfg_json))

    return render(request, 'interface/preferences.html', context)


def pause(request):
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

    id = request.GET["id"]
    key = request.GET["key"]

    cur.execute('SELECT * FROM "List" WHERE id=' + id + "AND key='" + key + "';")

    status = 0
    for i in cur.fetchall():
        status = i[3]

    conn.commit()
    conn.close()
    return JsonResponse({"status": status})


def set_pause(request):
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

    id = request.GET["id"]
    key = request.GET["key"]

    cur.execute('SELECT * FROM "List" WHERE id=' + id + "AND key='" + key + "';")

    status = 0
    for i in cur.fetchall():
        status = i[3]

    if status == 2:
        cur.execute('UPDATE "List" SET status = 1 WHERE id=' + id + "AND key='" + key + "';")
    elif status == 1:
        cur.execute('UPDATE "List" SET status = 2 WHERE id=' + id + "AND key='" + key + "';")

    conn.commit()
    conn.close()
    return JsonResponse({"answer": ""})


def delete(request):
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

    id = request.GET["id"]
    key = request.GET["key"]

    cur.execute('SELECT * FROM "List" WHERE id=' + id + "AND key='" + key + "';")

    status = 0
    for i in cur.fetchall():
        status = i[3]

    if status != 0:
        cur.execute('UPDATE "List" SET status = 0 WHERE id=' + id + "AND key='" + key + "';")

    conn.commit()
    conn.close()
    return JsonResponse({"answer": ""})