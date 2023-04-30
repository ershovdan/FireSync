# Fire Sync 🔥
Fire Sync is a server side of file syncer system. It consists from 
the core (created with Java), web server (Python, JS) for handling for file requests 
and user interface and database (PostgreSQL).

Works on Linux, MacOS.

---
## Installation
To make your FireSync ready for usage you need some things. 
- Internet connection - 
it needed to install some tools and give some js code opportunity to work.
- Java. Current version of FireSync uses JDK 18.
- PostgreSQL. Since FS's DB powered by postgres you need it. 
After the installation you have to create DB and user for FireSync. 
Names "FireSync" for db and "fsync" for user recommended, but you can 
create your own ones and then set it in settings; anyway, there you will
set host, port and password.
- If you want to use FireSync not only in LAN, then setup your DDNS.


## Usage

### Interface
The interface contains 2 main parts. Interaction panel is the first one, 
it consists of 4 pages: home, list, add, settings. On the other hand second 
panel or right menu shows you some statistics about operations acting 
right now.

Clicking by name of share at list page you will go to the page with extended 
info and settings for that share.

### Host-Client interactions
Each share has name, unique id (int) and access key (16 symbols long hex string). Client can access a share
only knowing id and key. Share's id is public, so it cannot cause any
security issues; at the same time, key is private. Therefore, client needs to 
have both of them to get data from host.

Information about share's files located in specific json files. Client
can get them by requesting `<host>/files/get_json/?id=<share id>&key=<share key>`.
it will redirect to json file with given id and key or return `{"answer": "false"}`
in the case of an error.

> Webserver returns `{"answer": "false"}` after a request with incorrect inputs

Every file of share is stored as separated zip files with unique id.

Then client has info about share's files. Json file structure looks like:
`{"all_files":{<file's rel path>: <file id>},"time":<time of last update>,"unzippedSize":<size>,"zippedSize":<size>}`.
In "all_files" you can find relation between all paths of file and id (in hex format).

Knowing share id, key and file id client can exactly get file by requesting `<host>/files/get_file/?id=<share id>&key=<share key>&file_id=<file id>`.
It will redirect him to needed zip file.

You can also find size of zipped and unzipped share, time of last update there.

### First run
After the first start FireSync will create "FireSyncData" directory in
your home catalog. It contains zips, configs, scripts, logs and webserver.

By default, webserver runs on port 8000, but your can change it in setting.

FS will set up db by itself.

### CLI [not ready]
If you don't want to use the default graphic interface, cli will 
give you an opportunity to create your own one. For more details see 
"How it works?" unit.

## How it works?

### Core
Core works in few threads (high freq and low freq). The first type manages 
db and webserver, it does it every 800 msec. Low freq threads collects statistics 
and looks for file changes (every 10 sec). Timeout class calls other classes
for their aims. 

There are some checker classes that do separated tasks. Core is the main class.
Init inits db, etc. Those classes use some tools located in "tools" package.

All statistic and data about shares put in db, except network usage due to
the iftop features.

Core checks every share files and add or delete some of them if it needed.

### Webserver
FireSync's webserver powered by Django. It has own virtual environment, 
therefore you have no need to manage it.

WebServer has 2 applications (files and interface). The first one manages 
file requests handling, otherwise "interface" works with interface and 
connect it with db.

Finally, webserver exchanges data between gui and db.

### Data Base
It powered with PostgresSQL. DB contains next tables:

#### List
- id
- name
- path
- statuses
  - 0 - disabled/hidden
  - 1 - active
  - 2 - paused
  - 4 - zipping
  - 5 - just created
- key

#### Connected
 - time 
 - amount
 - id

#### ConnectedBuffer
- time
- id

#### Network
- time
- data

#### Other
- type
- value_int
- value_str

### Statistics collection

- Network usage manager uses iftop as a data provider (`/operations_get_data?check=<network_5 or network_15 or network_30 or network_60>`). Returns time-data JSON dict.
- Total shares - just counts quantity of shares (`/operations_get_data?check=total_shares`). Returns JSON dict `{"answer": <quantity>}`.
- Users using given share now (`/operations_get_data?check=<connected_users_5 or connected_users_30 or connected_users_60>&id=<share id>&key=<share key>`). Represents amount of requests by last time period. Returns time-data JSON dict.
- Users using all shares now (`/operations_get_data?check=<connected_users_all_5 or connected_users_all_30 or connected_users_all_60>&id=<share id>&key=<share key>`). It's a sum of all amounts of shares. Returns time-data JSON dict.
- Share which is processing at current time (`/operations_get_data?check=now_operating`). Returns JSON dict `{"answer": <name>}`.
- Share status (`/operations_get_data?check=status&id=<share id>&key=<share key>`). Returns JSON dict `{"answer": <status>}`.

### Requests for manage of shares
- Set pause (`/set_pause?&id=<share id>&key=<share key>`). Returns JSON dict `{"answer": <status>}`.
- If paused (`/pause?&id=<share id>&key=<share key>`). Returns JSON dict `{"answer": <status>}`.- If paused (`/pause?&id=<share id>&key=<share key>`). Returns JSON dict `{"answer": <status>}`.
- Delete (`/delete?&id=<share id>&key=<share key>`). Returns JSON dict `{"answer": ""}`.

### FireSyncData directory
This directory located in user's home catalog.
- "buffer" contains zipped files of shares and json info about them.
- "cfg" is the config dir (main.cfg, db.cfg).
- "db" consists from data for right_menu and iftop data.
- "scripts" - shell scripts
- "web_server" contains webserver
