#!/usr/bin/python
# -*- coding: utf-8 -*-

import json
import random
import datetime

def generate_data(tot):
    data = []
    today = datetime.datetime.utcnow()

    for i in range(0,tot):
        measure = {}
        measure['datetime']      = (today + datetime.timedelta(seconds=15*60*i)).strftime('%Y-%m-%dT%H:%M:%SZ')
        measure['soil_moisture'] = round(random.uniform(30, 90),1)
        measure['temperature']   = round(random.uniform(18, 22),1)
        measure['luminosity']    = int(random.uniform(900, 1100))
        measure['water_level']   = int(80 - 40*i/tot + random.uniform(-20/tot, 20/tot))
        measure['plant_watered'] = True if random.uniform(0, 1) >= 0.9 else False

        data.append(measure)

    return data

def generate_notifications():
    return [
        {
            'text': 'Plant watered!',
            'level': 'info'
        },
        {
            'text': 'Water level low :(',
            'level': 'alert'
        }
    ]

def generate_configuration():
    return {
        'target_soil_moisture': 50,
        'water_volume_pumped': 250,
        'logging_interval': 900,
        'sending_interval': 3600
    }

def generate_commands():
    return [
        {
            'type': 'water'
        }
    ]

with open('pot-server.json', 'w+') as f:
    json_content = {}
    json_content['data'] = generate_data(4)
    f.write(json.dumps(json_content, sort_keys=True, indent=4))

with open('server-smartphone.json', 'w+') as f:
    json_content = {}
    json_content['data'] = generate_data(4*24)
    json_content['notifications'] = generate_notifications()
    json_content['configuration'] = generate_configuration()
    f.write(json.dumps(json_content, sort_keys=True, indent=4))

with open('smartphone-server.json', 'w+') as f:
    json_content = {}
    json_content['configuration'] = generate_configuration()
    json_content['commands'] = generate_commands()
    f.write(json.dumps(json_content, sort_keys=True, indent=4))

with open('server-pot.json', 'w+') as f:
    json_content = {}
    json_content['configuration'] = generate_configuration()
    json_content['commands'] = generate_commands()
    f.write(json.dumps(json_content, sort_keys=True, indent=4))
