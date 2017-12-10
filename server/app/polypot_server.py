#!/usr/bin/python
# -*- coding: utf-8 -*-

import datetime
import uuid

from flask import Flask, request, jsonify, render_template
from polypot_database import *

app = Flask(__name__)
app.config.from_envvar('POLYPOT_SETTINGS')

@app.route('/')
def index():
    stats = {}
    stats['pots'] = Config.query.count()
    stats['measures'] = Measure.query.count()
    stats['commands'] = Command.query.count()
    return render_template('index.html', stats=stats)

@app.route('/setup', methods=['POST'])
def setup():
    json_content = request.get_json()

    # Generate new UUID
    pot_id = uuid.uuid4()
    n = Config.query.filter_by(pot_id=pot_id.hex).count()
    while n > 0:
        pot_id = uuid.uuid4()
        n = Config.query.filter_by(pot_id=pot_id.hex).count()

    # Store configuration and uuid
    config = Config()
    config.pot_id = pot_id.hex
    config.from_dict(json_content['configuration'])
    db.session.add(config)

    # Commit all changes
    db.session.commit()

    # Send uuid
    json_content = {}
    json_content['uuid'] = str(pot_id)

    return jsonify(json_content)

def add_measure(m, pot_id):
    measure = Measure()
    measure.from_dict(m)
    measure.pot_id=pot_id
    db.session.add(measure)

def add_command(c, pot_id):
    id = c.get('id', None)

    if id != None:
        command = Command.query.filter(Command.pot_id == pot_id, Command.id == id).one()
    else:
        command = Command()

    command.from_dict(c)
    command.pot_id=pot_id
    db.session.add(command)

@app.route('/send-data/<uuid:pot_id>', methods=['POST'])
def send_data(pot_id):
    json_content = request.get_json()

    # Store data
    if 'data' in json_content:
        if type(json_content['data']) == list:
            for m in json_content['data']:
                add_measure(m, pot_id.hex)
        elif type(json_content['data']) == dict:
            add_measure(json_content['data'], pot_id.hex)

    # Store commands
    if 'commands' in json_content:
        if type(json_content['commands']) == list:
            for c in json_content['commands']:
                add_command(c, pot_id.hex)
        elif type(json_content['commands']) == dict:
            add_command(json_content['commands'], pot_id.hex)

    # Send commands & configuration
    json_content = {}

    config = Config.query.filter_by(pot_id=pot_id.hex).one()
    json_content['configuration'] = config.to_dict()

    json_content['commands'] = []
    commands = Command.query.filter(Command.pot_id == pot_id.hex, Command.status == CommandStatus.new).all()
    for command in commands:
        json_content['commands'].append(command.to_dict())
        command.status = CommandStatus.sent

    # Commit all changes
    db.session.commit()

    return jsonify(json_content)

@app.route('/get-latest/<uuid:pot_id>')
def get_latest(pot_id):
    json_content = {}

    # Send last data point
    measure = Measure.query.filter_by(pot_id=pot_id.hex).order_by(Measure.datetime.desc()).first()
    json_content['data'] = measure.to_dict() if measure else {}

    # Send configuration
    config = Config.query.filter_by(pot_id=pot_id.hex).one()
    json_content['configuration'] = config.to_dict()

    # Send the last executed command of each type
    json_content['commands'] = []
    for type in CommandType:
        command = Command.query.filter(Command.pot_id == pot_id.hex, Command.type == type, Command.status == CommandStatus.executed).order_by(Command.datetime.desc()).first()
        if command:
            json_content['commands'].append(command.to_dict())

    response = jsonify(json_content)
    response.cache_control.max_age = config.sending_interval
    return response

@app.route('/get-data/<uuid:pot_id>')
def get_data(pot_id):
    json_content = {}

    begin = request.args.get('from')
    end   = request.args.get('to')

    # Send back date range
    json_content['from'] = begin
    json_content['to'] = end

    begin = datetime.datetime.strptime(begin, '%Y-%m-%dT%H:%M:%SZ') if begin else datetime.datetime(datetime.MINYEAR, 1, 1, 0, 0, 0)
    end   = datetime.datetime.strptime(end, '%Y-%m-%dT%H:%M:%SZ')   if end   else datetime.datetime(datetime.MAXYEAR, 12, 31, 23, 59, 59)

    # Send data stored
    json_content['data'] = []
    measures = Measure.query.filter(Measure.pot_id == pot_id.hex, Measure.datetime >= begin, Measure.datetime < end).order_by(Measure.datetime.asc()).all()
    for measure in measures:
        json_content['data'].append(measure.to_dict())

    # Send commands
    json_content['commands'] = []
    commands = Command.query.filter(Command.pot_id == pot_id.hex, Command.datetime >= begin, Command.datetime < end).order_by(Command.datetime.asc()).all()
    for command in commands:
        json_content['commands'].append(command.to_dict())

    # Get configuration
    config = Config.query.filter_by(pot_id=pot_id.hex).one()

    response = jsonify(json_content)
    response.cache_control.max_age = config.sending_interval
    return response

@app.route('/send-c-and-c/<uuid:pot_id>', methods=['POST'])
def send_commands_and_configuration(pot_id):
    json_content = request.get_json()

    # Store configuration
    if 'configuration' in json_content:
        config = Config.query.filter_by(pot_id=pot_id.hex).one()
        config.from_dict(json_content['configuration'])

    # Store commands
    if 'commands' in json_content:
        if type(json_content['commands']) == list:
            for c in json_content['commands']:
                add_command(c, pot_id.hex)
        elif type(json_content['commands']) == dict:
            add_command(json_content['commands'], pot_id.hex)

    # Commit all changes
    db.session.commit()

    # Send back empty JSON response
    json_content = {}

    return jsonify(json_content)


db.init_app(app)
db.create_all(app=app)
app.run(host='0.0.0.0')
