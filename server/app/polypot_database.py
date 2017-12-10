#!/usr/bin/python
# -*- coding: utf-8 -*-

import datetime
import enum

from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class WaterTank(enum.Enum):
    standard = 'Standard'

class Plant(enum.Enum):
    unspecified = 'Unspecified'
    none        = 'None'

class Config(db.Model):
    id                   = db.Column(db.Integer, primary_key=True)
    pot_id               = db.Column(db.String(length=32), nullable=False, index=True, unique=True)
    target_soil_moisture = db.Column(db.Float, default=50, nullable=False)
    water_volume_pumped  = db.Column(db.Integer, default=250, nullable=False)
    logging_interval     = db.Column(db.Integer, default=900, nullable=False)
    sending_interval     = db.Column(db.Integer, default=3600, nullable=False)
    water_tank           = db.Column(db.Enum(WaterTank), default=WaterTank.standard, nullable=False)
    plant                = db.Column(db.Enum(Plant), default=Plant.unspecified, nullable=False)

    def from_dict(self, d):
        self.target_soil_moisture = d.get('target_soil_moisture', self.target_soil_moisture) # update only if needed
        self.water_volume_pumped  = d.get('water_volume_pumped', self.water_volume_pumped)   # update only if needed
        self.logging_interval     = d.get('logging_interval', self.logging_interval)         # update only if needed
        self.water_tank           = WaterTank(d.get('water_tank', self.water_tank))          # update only if needed
        self.plant                = Plant(d.get('plant', self.plant))                        # update only if needed

    def to_dict(self):
        d = {}
        d['target_soil_moisture'] = self.target_soil_moisture
        d['water_volume_pumped']  = self.water_volume_pumped
        d['logging_interval']     = self.logging_interval
        d['sending_interval']     = self.sending_interval
        d['water_tank']           = self.water_tank.value
        d['plant']                = self.plant.value
        return d

class Measure(db.Model):
    id            = db.Column(db.Integer, primary_key=True)
    pot_id        = db.Column(db.String(length=32), nullable=False, index=True)
    datetime      = db.Column(db.DateTime, nullable=False)
    soil_moisture = db.Column(db.Float, nullable=False)
    temperature   = db.Column(db.Float, nullable=False)
    luminosity    = db.Column(db.Float, nullable=False)
    water_level   = db.Column(db.Float, nullable=False)
    battery_level = db.Column(db.Float, nullable=False)

    def from_dict(self, d):
        self.datetime      = datetime.datetime.strptime(d['datetime'], '%Y-%m-%dT%H:%M:%SZ') # mandatory
        self.soil_moisture = d['soil_moisture'] # mandatory
        self.temperature   = d['temperature']   # mandatory
        self.luminosity    = d['luminosity']    # mandatory
        self.water_level   = d['water_level']   # mandatory
        self.battery_level = d['battery_level'] # mandatory

    def to_dict(self):
        d = {}
        d['datetime']      = self.datetime.strftime('%Y-%m-%dT%H:%M:%SZ')
        d['soil_moisture'] = self.soil_moisture
        d['temperature']   = self.temperature
        d['luminosity']    = self.luminosity
        d['water_level'] = self.water_level
        d['battery_level'] = self.battery_level
        return d

class CommandType(enum.Enum):
    water = 'water'

class CommandStatus(enum.Enum):
    new      = 'new'
    sent     = 'sent'
    executed = 'executed'

class Command(db.Model):
    id       = db.Column(db.Integer, primary_key=True)
    pot_id   = db.Column(db.String(length=32), nullable=False, index=True)
    type     = db.Column(db.Enum(CommandType), nullable=False)
    status   = db.Column(db.Enum(CommandStatus), nullable=False)
    datetime = db.Column(db.DateTime, nullable=False)
    args     = db.Column(db.PickleType, nullable=True)

    def from_dict(self, d):
        self.id       = d.get('id', None)          # optional
        self.type     = CommandType(d['type'])     # mandatory
        self.status   = CommandStatus(d['status']) # mandatory
        self.datetime = datetime.datetime.strptime(d['datetime'], '%Y-%m-%dT%H:%M:%SZ') # mandatory
        self.args     = d.get('args', None)        # optional

    def to_dict(self):
        d = {}
        d['id']       = self.id
        d['type']     = self.type.value
        d['status']   = self.status.value
        d['datetime'] = self.datetime.strftime('%Y-%m-%dT%H:%M:%SZ')
        d['args']     = self.args
        return d
