from django.shortcuts import render
import pathlib
import sqlite3
import os


module_dir = pathlib.Path(os.path.dirname(__file__))
file_path = os.path.join(module_dir.parent.parent.parent, 'db', 'operations.db')


def home(request):
    return render(request, 'interface/home.html')


def add(request):
    if request.method == "POST":
        con = sqlite3.connect(file_path)
        cur = con.cursor()

        cur.execute('INSERT INTO List(name, path, status) VALUES("' + request.POST['name'] + '", "' + request.POST['path'] + '", 1);')
        con.commit()

        print(request.POST)
    return render(request, 'interface/add.html')

