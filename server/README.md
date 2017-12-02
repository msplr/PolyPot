# PolyPot Server

## Intro

The PolyPot Server is a Python app written using Flask and SQLAlchemy.

## Run it!

You can run it on your local machine using the following commands:

```
virtualenv ~/.virtualenv/polypot
source ~/.virtualenv/polypot/bin/activate
cd app
pip install -r requirements.txt
PYTHONDONTWRITEBYTECODE=True POLYPOT_SETTINGS=config-dev.py python polypot_server.py
```

## Docker

Or using Docker:

```
docker build -t polypot:latest .
docker run --rm -it -p 127.0.0.1:5000:5000 -v $(pwd):/polypot/db:Z polypot
```

## Testing

You can test the server using the following commands:

```
curl -v localhost:5000/setup/01234567-89ab-cdef-0123-456789abcdef
curl -v localhost:5000/send-c-and-c/01234567-89ab-cdef-0123-456789abcdef -d "@../protocol/smartphone-server.json" -H "Content-Type: application/json"
curl -v localhost:5000/send-data/01234567-89ab-cdef-0123-456789abcdef -d "@../protocol/pot-server.json" -H "Content-Type: application/json"
curl -v localhost:5000/get-latest/01234567-89ab-cdef-0123-456789abcdef
curl -v localhost:5000/get-data/01234567-89ab-cdef-0123-456789abcdef
```
