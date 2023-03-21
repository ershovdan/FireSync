import os
import sqlite3


file_path = "/Users/ershovdan/Programming/FireSync/out/production/db/operations.db"

def activeShares():
    con = sqlite3.connect(file_path)
    cur = con.cursor()
    cur.execute('SELECT * FROM List WHERE status = 1;')
    l = len(cur.fetchall())
    con.close()
    return str(l)