port=$1
password=$2

# optional
docker pull postgres

docker run -d -p $port:5432 --name FireSyncPostgres -e POSTGRES_PASSWORD=$password postgres

cd ~/
mkdir "FireSyncData" > /dev/null
cd FireSyncData
mkdir "cfg" > /dev/null
cd cfg
touch db.cfg

echo "" >> db.cfg
echo "{\"name\": \"postgres\", \"port\": \"${port}\", \"user\": \"postgres\", \"password\": \"${password}\", \"host\": \"localhost\"}" >> db.cfg
