/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/declare",
    "geometria/GMath"
], function(declare, math) {

    var MIN_SPIN = 7e-3;
    var SPIN_FREQUENCY = 50; // Hz
    var SPIN_SENSITIVITY = 0.8; // 0 to 1
    
    var fromAxisAngle = function(axis, angle) {
        var m4 = mat4.create();
        mat4.rotate(m4, m4, angle, axis);
        var m3 = mat3.create();
        mat3.fromMat4(m3, m4);
        return m3;
    };

    var normalize = function(m) {
        var q = quat.create();
        quat.fromMat3(q, m);
        quat.normalize(q, q);
        return mat3.fromQuat(m, q);
    }
    
    return declare(null, {
    
        constructor: function(figure) {
            this.initialAttitude = this.getDefaultAttitude();
            this.attitude = null;
            this.spinner = null;
            this._seized = null;
            this._figure = figure;
            this.toDefaultAttitude();
        },

        make: function(props) {
            if (!math.validateQuat(props)) {
                throw "";
            }
            var q = quat.normalize([], props);
            mat3.fromQuat(this.initialAttitude, q);
            this.toInitialAttitude();
            return this;
        },

        getDefaultAttitude: function() {
            var m1 = fromAxisAngle([1, 1, 1], -Math.PI*2/3);
            var m2 = fromAxisAngle([0, 1, 0], Math.PI/7);
            var m3 = fromAxisAngle([1, 0, 0], Math.PI/15);
            mat3.mul(m2, m2, m1);
            mat3.mul(m3, m3, m2);
            return normalize(m3);
        },

        toDefaultAttitude: function() {
            this.attitude = this.getDefaultAttitude();
        },
        
        toInitialAttitude: function() {
            this.attitude = mat3.copy(this.attitude, this.initialAttitude);
        },
        
        isFaceVisible: function(face, solid, refPoint) {
            var on = vec3.clone(face.getRefNormal(solid, refPoint));
            vec3.transformMat3(on, on, this.attitude);
            return on[2] > 0;
        },
        
        turn: function(axis, length) {
            var rotMatrix = fromAxisAngle(axis, length * MIN_SPIN);
            mat3.mul(rotMatrix, rotMatrix, this.attitude);
            this.attitude = normalize(rotMatrix);
            this._figure.draw();
        },
        
        spin: function(axis, stroke) {
            var camera = this;
            camera._seized = false;
            camera.spinner = window.setInterval(function() {
                if (camera._seized) {
                    clearInterval(camera.spinner);
                    return false;
                }
                camera.turn(axis, stroke * SPIN_SENSITIVITY);
            }, 1 / SPIN_FREQUENCY);
        },
        
        seize: function() {
            this._seized = true;
        },

        toJson: function() {
            var q = quat.fromMat3([], this.attitude);
            var json = [q[0], q[1], q[2], q[3]];
            return json;
        }
    });
});
