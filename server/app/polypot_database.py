#!/usr/bin/python
# -*- coding: utf-8 -*-

import datetime

from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class Config(db.Model):
    id                   = db.Column(db.Integer, primary_key=True)
    pot_id               = db.Column(db.String(length=32), nullable=False, index=True, unique=True)
    target_soil_moisture = db.Column(db.Float, default=50, nullable=False)
    water_volume_pumped  = db.Column(db.Integer, default=150, nullable=False)
    logging_interval     = db.Column(db.Integer, default=900, nullable=False)
    sending_interval     = db.Column(db.Integer, default=3600, nullable=False)

    def from_dict(self, d):
        self.target_soil_moisture = d.get('target_soil_moisture', self.target_soil_moisture) # update only if needed
        self.water_volume_pumped  = d.get('water_volume_pumped', self.water_volume_pumped)   # update only if needed
        self.logging_interval     = d.get('logging_interval', self.logging_interval)         # update only if needed
        self.sending_interval     = d.get('sending_interval', self.sending_interval)         # update only if needed

    def to_dict(self):
        d = {}
        d['target_soil_moisture'] = self.target_soil_moisture
        d['water_volume_pumped']  = self.water_volume_pumped
        d['logging_interval']     = self.logging_interval
        d['sending_interval']     = self.sending_interval
        return d

class Measure(db.Model):
    id            = db.Column(db.Integer, primary_key=True)
    pot_id        = db.Column(db.String(length=32), nullable=False, index=True)
    datetime      = db.Column(db.DateTime, nullable=False)
    soil_moisture = db.Column(db.Float, nullable=False)
    temperature   = db.Column(db.Float, nullable=False)
    luminosity    = db.Column(db.Integer, nullable=False)
    water_level   = db.Column(db.Integer, nullable=False)
    plant_watered = db.Column(db.Boolean, nullable=False)

    def from_dict(self, d):
        self.datetime      = datetime.datetime.strptime(d['datetime'], '%Y-%m-%dT%H:%M:%SZ') # mandatory
        self.soil_moisture = d['soil_moisture'] # mandatory
        self.temperature   = d['temperature']   # mandatory
        self.luminosity    = d['luminosity']    # mandatory
        self.water_level   = d['water_level']   # mandatory
        self.plant_watered = d['plant_watered'] # mandatory

    def to_dict(self):
        d = {}
        d['datetime']      = self.datetime.strftime('%Y-%m-%dT%H:%M:%SZ')
        d['soil_moisture'] = self.soil_moisture
        d['temperature']   = self.temperature
        d['luminosity']    = self.luminosity
        d['water_level']   = self.water_level
        d['plant_watered'] = self.plant_watered
        return d

class Command(db.Model):
    id     = db.Column(db.Integer, primary_key=True)
    pot_id = db.Column(db.String(length=32), nullable=False, index=True)
    type   = db.Column(db.Text, nullable=False)
    args   = db.Column(db.PickleType, nullable=True)

    def from_dict(self, d):
        self.type = d['type']           # mandatory
        self.args = d.get('args', None) # optional

    def to_dict(self):
        d = {}
        d['type'] = self.type
        d['args'] = self.args
        return d
