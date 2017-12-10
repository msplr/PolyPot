#!/usr/bin/python
# -*- coding: utf-8 -*-

import random
import datetime
import uuid
import math

import click
from flask import Flask
from polypot_database import *

app = Flask(__name__)
app.config.from_envvar('POLYPOT_SETTINGS')

def generate_data(length, interval, pot_id):
    first_day = datetime.datetime.utcnow()
    first_day = first_day + datetime.timedelta(seconds=-length)
    first_day = first_day.replace(hour=0, minute=0, second=0, microsecond=0)

    config = Config()
    config.pot_id = pot_id.hex
    db.session.add(config)
    db.session.commit()

    previous_soil_moisture = 100

    for i in range(0,int(length/interval)):
        current_day = first_day + datetime.timedelta(seconds=15*60*i)
        seconds_elapsed  = current_day.timestamp() - first_day.timestamp()

        seconds_one_day = 24*60*60
        seconds_between_watering = 5.2*24*60*60

        measure = Measure()

        measure.pot_id        = pot_id.hex
        measure.datetime      = current_day
        measure.soil_moisture =   80 -  50*(seconds_elapsed/seconds_between_watering % 1)                    + 2.5*random.uniform(-1,1)
        measure.temperature   =   20 +   2*math.sin(2*math.pi/seconds_one_day * seconds_elapsed +  6*60*60)  + 0.5*random.uniform(-1,1)
        measure.luminosity    =  400 + 400*math.sin(2*math.pi/seconds_one_day * seconds_elapsed +  6*60*60)  +  20*random.uniform(-1,1)
        measure.water_level   = ( 90 -  20*math.floor(seconds_elapsed/seconds_between_watering)              + 0.5*random.uniform(-1,1)) % 100
        measure.battery_level =   50

        if(previous_soil_moisture -  measure.soil_moisture < -20):
            command = Command()

            command.pot_id   = pot_id.hex
            command.type     = CommandType.water
            command.status   = CommandStatus.executed
            command.datetime = current_day

            db.session.add(command)

        previous_soil_moisture = measure.soil_moisture

        db.session.add(measure)

@app.cli.command()
def populate():
    generate_data((365*24 + 12)*60*60, 15*60, uuid.UUID('{01234567-89ab-cdef-0123-456789abcdef}'))
    generate_data(                  0, 15*60, uuid.UUID('{00000000-0000-0000-0000-000000000000}'))

    db.session.commit()

db.init_app(app)
db.create_all(app=app)
