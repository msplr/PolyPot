#!/usr/bin/python
# -*- coding: utf-8 -*-

import datetime

from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config.from_envvar('POLYPOT_SETTINGS')

db = SQLAlchemy(app)

#TODO: general: errors management

class Measure(db.Model):
    id            = db.Column(db.Integer, primary_key=True)
    datetime      = db.Column(db.DateTime(), nullable=False) #TODO: unique?
    soil_moisture = db.Column(db.Float(),nullable=False)
    temperature   = db.Column(db.Float(),nullable=False)
    luminosity    = db.Column(db.Integer(),nullable=False)
    water_level   = db.Column(db.Integer(),nullable=False)
    plant_watered = db.Column(db.Boolean(),nullable=False)
        
    def to_dict(self):
        d = {}
        d['datetime']      = self.datetime.strftime('%Y-%m-%dT%H:%M:%SZ')
        d['soil_moisture'] = self.soil_moisture
        d['temperature']   = self.temperature
        d['luminosity']    = self.luminosity
        d['water_level']   = self.water_level
        d['plant_watered'] = self.plant_watered
        return d
    
    def from_dict(self, d):
        self.datetime      = datetime.datetime.strptime(d['datetime'], '%Y-%m-%dT%H:%M:%SZ')
        self.soil_moisture = d['soil_moisture']
        self.temperature   = d['temperature']
        self.luminosity    = d['luminosity']
        self.water_level   = d['water_level']
        self.plant_watered = d['plant_watered']

@app.route('/get-data')
def get_data():
    # Send all data stored #TODO: limit in time or number? pass day in param?
    json_content = {}
    json_content['data'] = []
    
    for measure in Measure.query.order_by(Measure.datetime.asc()).all():
        json_content['data'].append(measure.to_dict())
    
    # Send notifications
    #TODO
    
    return jsonify(json_content)

@app.route('/send-data', methods=['POST'])
def send_data():
    # Save sent data
    json_content = request.get_json()
    for measure in json_content['data']:
        m = Measure()
        m.from_dict(measure)
        
        db.session.add(m)
    db.session.commit()
    
    # Send commands & configuration
    #TODO
    return 'Data added'

print(__name__)

if __name__ == '__main__':
    db.create_all()
    app.run(host='0.0.0.0')
