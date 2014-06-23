/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */
define([
    "dojo/_base/Color",
    "dojox/widget/Standby",
    "geometria/GDictionary",
    "geometria/GMainContainer",
    "geometria/GMath"
], function(Color, Standby, dict, mainContainer, math) {

    var mjs = mathjs();
    var standby;
    var uid = 0;
    
    return {

        labelRegex: /^([A-Z])([0-9]*)$/,
        variableRegex: /^[a-zA-Z_]\w*$/,
        figureNameRegex: /^[a-zA-Z_]\w*$/,
        crdsRegex: /^\[(\S+)\s+(\S+)\s+(\S+)\]$/,
        homeUrl: "http://geocentral.net/geometria",
        zoomFactor: Math.sqrt(2),
        pointRadius: 3,
        selectedPointRadius: 4,
        pointColor: "red",
        stickColor: "black",
        faceColor: "#00ffff",
        paintedLineColor: "white",
        selectionColor: "#aa00aa",
        selectionStrokeWidth: 4,
        selectEpsilon: 5,
        strokeDashArray: "5, 3",
        figureMargin: 0.2,

        getUid: function() {
            return (uid++).toString();
        },
        
        eval: function(expression, scope) {
            var value = NaN;
            try {
                value = mjs.eval(expression, scope);
            }
            catch (err) {}
            return value;
        },
        
        propCount: function(obj) {
            var count = 0;
            $.each(obj, function() {
                count++;
            });
            return count;
        },
        
        anyProp: function(obj) {
            var prop;
            $.each(obj, function() {
                prop = this;
                return false;
            });
            return prop;
        },

        firstKey: function(obj) {
            var key;
            $.each(obj, function(k) {
                key = k;
                return false;
            });
            return key;
        },
        
        getNewLabel: function(solid) {
            var labelRegex = this.labelRegex;
            var maxSuffix = 0;
            var prefixAtMaxSuffix = "A";
            $.each(solid.relatedSolids, function() {
                $.each(this.points, function(label, p) {
                    var match = new RegExp(labelRegex).exec(label);
                    var prefix = match[1];
                    var suffix = parseInt(match[2]) ;
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                        prefixAtMaxSuffix = prefix;
                    }
                    else if ((!maxSuffix || suffix == maxSuffix) && prefix > prefixAtMaxSuffix) {
                        prefixAtMaxSuffix = prefix;
                    }
                });
            });
            var suffix;
            if (prefixAtMaxSuffix < "Z") {
                var prefix = String.fromCharCode(prefixAtMaxSuffix.charCodeAt(0) + 1);
                suffix = maxSuffix ? maxSuffix : "";
                return prefix + suffix;
            }
            else {
                return "A" + (maxSuffix + 1);
            }
        },
        
        getNextLabel: function(label) {
            if (!label) {
                return "A";
            }
            var match = new RegExp(this.labelRegex).exec(label);
            var prefix = match[1];
            var suffix = match[2];
            if (prefix < "Z") {
                var newPrefix = String.fromCharCode(prefix.charCodeAt(0) + 1);
                return newPrefix + suffix;
            }
            if (!suffix.length) {
                suffix = "1";
            }
            else {
                suffix = parseInt(suffix) + 1;
            }
            return "A" + suffix;
        },

        getNewFigureName: function() {
            var names = [];
            $.each(mainContainer.currentDocument.figures, function() {
                names.push(this.name);
            });
            var prefix = dict.get("Figure");
            var prefixLength = prefix.length;
            var suffixes = [];
            $.each(names, function(index, name) {
                if (name.indexOf(prefix) == 0 && name.length > prefixLength) {
                    var s = name.substring(prefixLength);
                    suffixes.push(parseInt(s));
                }
            });
            suffixes.sort();
            var suffix = suffixes.length ? suffixes[suffixes.length - 1] + 1 : 1;
            return prefix + suffix;
        },

        getLabels: function(stringValue) {
            var labels = [];
            var matches;
            var regex =/[A-Z][0-9]*/g;
            while (matches = regex.exec(stringValue)) {
                labels.push(matches[0]);
            }
            return labels;
        },
        
        remove: function(array, item) {
            var index = array.indexOf(item);
            if (index > -1) {
                array.splice(index, 1);
            }
        },
        
        removeAll: function(srcArray, targetArray) {
            return srcArray.filter(function(item) {
                return targetArray.indexOf(item) === -1;
            });
        },

        showStandby: function() {
            if (!standby) {
                var body = window.document.body;
                standby = new Standby({
                    target: body,
                    zIndex: 1000,
                    text: dict.get("PleaseWait"),
                    image: "images/loading.gif"
                });
                body.appendChild(standby.domNode);
                standby.startup();
            }
            standby.show();
        },
        
        hideStandby: function() {
            if (standby) {
                standby.hide();
            }
        },
        
        colorCssToHsb: function(colorCss) {
            var rgb = new Color(colorCss).toRgb();
            var r = rgb[0], g = rgb[1], b = rgb[2];
            var hue, saturation, brightness;
            var cmax = (r > g) ? r : g;
            if (b > cmax) {
                cmax = b;
            }
            var cmin = (r < g) ? r : g;
            if (b < cmin) {
                cmin = b;
            }
            brightness = cmax/255;
            if (cmax != 0) {
                saturation = (cmax - cmin)/cmax;
            }
            else {
                saturation = 0;
            }
            if (saturation == 0) {
                hue = 0;
            }
            else {
                var redc = (cmax - r)/(cmax - cmin);
                var greenc = (cmax - g)/(cmax - cmin);
                var bluec = (cmax - b)/(cmax - cmin);
                if (r == cmax) {
                    hue = bluec - greenc;
                }
                else if (g == cmax) {
                    hue = 2 + redc - bluec;
                }
                else {
                    hue = 4 + greenc - redc;
                }
                hue = hue/6;
                if (hue < 0) {
                    hue = hue + 1;
                }
            }
            return [hue, saturation, brightness];
        },
        
        colorHsbToCss: function(hsb) {
            var hue = hsb[0], saturation = hsb[1], brightness = hsb[2];
            var r = 0, g = 0, b = 0;
            if (saturation == 0) {
                r = g = b = Math.floor(brightness * 255 + 0.5);
            }
            else {
                var h = (hue - Math.floor(hue))*6;
                var f = h - Math.floor(h);
                var p = brightness*(1 - saturation);
                var q = brightness*(1 - saturation*f);
                var t = brightness*(1 - (saturation*(1 - f)));
                switch (Math.floor(h)) {
                case 0:
                r = Math.floor(brightness*255 + 0.5);
                g = Math.floor(t*255 + 0.5);
                b = Math.floor(p*255 + 0.5);
                break;
                case 1:
                r = Math.floor(q*255 + 0.5);
                g = Math.floor(brightness*255 + 0.5);
                b = Math.floor(p*255 + 0.5);
                break;
                case 2:
                r = Math.floor(p*255 + 0.5);
                g = Math.floor(brightness*255 + 0.5);
                b = Math.floor(t*255 + 0.5);
                break;
                case 3:
                r = Math.floor(p*255 + 0.5);
                g = Math.floor(q*255 + 0.5);
                b = Math.floor(brightness*255 + 0.5);
                break;
                case 4:
                r = Math.floor(t*255 + 0.5);
                g = Math.floor(p*255 + 0.5);
                b = Math.floor(brightness*255 + 0.5);
                break;
                case 5:
                r = Math.floor(brightness*255 + 0.5);
                g = Math.floor(p*255 + 0.5);
                b = Math.floor(q*255 + 0.5);
                break;
                }
            }
            var colorCss = new Color([r, g, b]).toHex();
            return colorCss;
        },
        
        resolveMacros: function(text) {
            var yearMacro = "${year}";
            var resolvedText = text;
            while (resolvedText.indexOf(yearMacro) > -1) {
                resolvedText = resolvedText.replace(yearMacro, new Date().getFullYear());
            }
            return resolvedText;
        },
        
        getQueryParams: function() {
            var params = {};
            var query = window.location.search;
            if (query) {
                $.each(query.slice(1).split("&"), function() {
                    var tokens = this.split("=");
                    var paramName = tokens[0].trim();
                    if (paramName.length > 0) {
                        var paramValue = tokens.length > 1 ? tokens[1].trim() : "";
                        params[paramName] = paramValue;
                    }
                });
            }
            return params;
        }
    };
});
