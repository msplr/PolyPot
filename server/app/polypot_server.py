#!/usr/bin/python
# -*- coding: utf-8 -*-

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

@app.route('/setup/<uuid:pot_id>')
def setup(pot_id):
    # Generate new configuration
    config = Config()
    config.pot_id = pot_id.hex
    db.session.add(config)

    # Commit all changes
    db.session.commit()

    # Send configuration and id
    json_content = {}
    json_content['configuration'] = config.to_dict()

    return jsonify(json_content)

def add_measure(m, pot_id):
    measure = Measure()
    measure.from_dict(m)
    measure.pot_id=pot_id
    db.session.add(measure)

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

    # Send commands & configuration
    json_content = {}
    json_content['configuration'] = Config.query.get(pot_id.hex).to_dict()
    json_content['commands'] = []

    for command in Command.query.filter_by(pot_id=pot_id.hex).all():
        json_content['commands'].append(command.to_dict())
        db.session.delete(command)

    # Commit all changes
    db.session.commit()

    return jsonify(json_content)

@app.route('/get-data/<uuid:pot_id>')
def get_data(pot_id):
    json_content = {}

    # Send all data stored
    json_content['data'] = []
    for measure in Measure.query.filter_by(pot_id=pot_id.hex).order_by(Measure.datetime.asc()).all():
        json_content['data'].append(measure.to_dict())

    # Send configuration
    json_content['configuration'] = Config.query.get(pot_id.hex).to_dict()

    return jsonify(json_content)

def add_command(c, pot_id):
    command = Command()
    command.from_dict(c)
    command.pot_id=pot_id
    db.session.add(command)

@app.route('/send-c-and-c/<uuid:pot_id>', methods=['POST'])
def send_commands_and_configuration(pot_id):
    json_content = request.get_json()

    # Store configuration
    if 'configuration' in json_content:
        config = Config.query.get(pot_id.hex)
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

    return 'DONE'

if __name__ == '__main__':
    db.init_app(app)
    db.create_all(app=app)
    app.run(host='0.0.0.0')
