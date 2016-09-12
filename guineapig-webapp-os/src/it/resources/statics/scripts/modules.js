/*!
 * URI.js - Mutating URLs
 *
 * Version: 1.13.2
 *
 * Author: Rodney Rehm
 * Web: http://medialize.github.io/URI.js/
 *
 * Licensed under
 *   MIT License http://www.opensource.org/licenses/mit-license
 *   GPL v3 http://opensource.org/licenses/GPL-3.0
 *
 */
(function (root, factory) {
  'use strict';
  // https://github.com/umdjs/umd/blob/master/returnExports.js
  if (typeof exports === 'object') {
    // Node
    module.exports = factory(require('./punycode'), require('./IPv6'), require('./SecondLevelDomains'));
  } else if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['./punycode', './IPv6', './SecondLevelDomains'], factory);
  } else {
    // Browser globals (root is window)
    root.URI = factory(root.punycode, root.IPv6, root.SecondLevelDomains, root);
  }
}(this, function (punycode, IPv6, SLD, root) {
  'use strict';
  /*global location, escape, unescape */
  // FIXME: v2.0.0 renamce non-camelCase properties to uppercase
  /*jshint camelcase: false */

  // save current URI variable, if any
  var _URI = root && root.URI;

  function URI(url, base) {
    // Allow instantiation without the 'new' keyword
    if (!(this instanceof URI)) {
      return new URI(url, base);
    }

    if (url === undefined) {
      if (typeof location !== 'undefined') {
        url = location.href + '';
      } else {
        url = '';
      }
    }

    this.href(url);

    // resolve to base according to http://dvcs.w3.org/hg/url/raw-file/tip/Overview.html#constructor
    if (base !== undefined) {
      return this.absoluteTo(base);
    }

    return this;
  }

  URI.version = '1.13.2';

  var p = URI.prototype;
  var hasOwn = Object.prototype.hasOwnProperty;

  function escapeRegEx(string) {
    // https://github.com/medialize/URI.js/commit/85ac21783c11f8ccab06106dba9735a31a86924d#commitcomment-821963
    return string.replace(/([.*+?^=!:${}()|[\]\/\\])/g, '\\$1');
  }

  function getType(value) {
    // IE8 doesn't return [Object Undefined] but [Object Object] for undefined value
    if (value === undefined) {
      return 'Undefined';
    }

    return String(Object.prototype.toString.call(value)).slice(8, -1);
  }

  function isArray(obj) {
    return getType(obj) === 'Array';
  }

  function filterArrayValues(data, value) {
    var lookup = {};
    var i, length;

    if (isArray(value)) {
      for (i = 0, length = value.length; i < length; i++) {
        lookup[value[i]] = true;
      }
    } else {
      lookup[value] = true;
    }

    for (i = 0, length = data.length; i < length; i++) {
      if (lookup[data[i]] !== undefined) {
        data.splice(i, 1);
        length--;
        i--;
      }
    }

    return data;
  }

  function arrayContains(list, value) {
    var i, length;

    // value may be string, number, array, regexp
    if (isArray(value)) {
      // Note: this can be optimized to O(n) (instead of current O(m * n))
      for (i = 0, length = value.length; i < length; i++) {
        if (!arrayContains(list, value[i])) {
          return false;
        }
      }

      return true;
    }

    var _type = getType(value);
    for (i = 0, length = list.length; i < length; i++) {
      if (_type === 'RegExp') {
        if (typeof list[i] === 'string' && list[i].match(value)) {
          return true;
        }
      } else if (list[i] === value) {
        return true;
      }
    }

    return false;
  }

  function arraysEqual(one, two) {
    if (!isArray(one) || !isArray(two)) {
      return false;
    }

    // arrays can't be equal if they have different amount of content
    if (one.length !== two.length) {
      return false;
    }

    one.sort();
    two.sort();

    for (var i = 0, l = one.length; i < l; i++) {
      if (one[i] !== two[i]) {
        return false;
      }
    }

    return true;
  }

  URI._parts = function() {
    return {
      protocol: null,
      username: null,
      password: null,
      hostname: null,
      urn: null,
      port: null,
      path: null,
      query: null,
      fragment: null,
      // state
      duplicateQueryParameters: URI.duplicateQueryParameters,
      escapeQuerySpace: URI.escapeQuerySpace
    };
  };
  // state: allow duplicate query parameters (a=1&a=1)
  URI.duplicateQueryParameters = false;
  // state: replaces + with %20 (space in query strings)
  URI.escapeQuerySpace = true;
  // static properties
  URI.protocol_expression = /^[a-z][a-z0-9.+-]*$/i;
  URI.idn_expression = /[^a-z0-9\.-]/i;
  URI.punycode_expression = /(xn--)/i;
  // well, 333.444.555.666 matches, but it sure ain't no IPv4 - do we care?
  URI.ip4_expression = /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/;
  // credits to Rich Brown
  // source: http://forums.intermapper.com/viewtopic.php?p=1096#1096
  // specification: http://www.ietf.org/rfc/rfc4291.txt
  URI.ip6_expression = /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/;
  // expression used is "gruber revised" (@gruber v2) determined to be the
  // best solution in a regex-golf we did a couple of ages ago at
  // * http://mathiasbynens.be/demo/url-regex
  // * http://rodneyrehm.de/t/url-regex.html
  URI.find_uri_expression = /\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))/ig;
  URI.findUri = {
    // valid "scheme://" or "www."
    start: /\b(?:([a-z][a-z0-9.+-]*:\/\/)|www\.)/gi,
    // everything up to the next whitespace
    end: /[\s\r\n]|$/,
    // trim trailing punctuation captured by end RegExp
    trim: /[`!()\[\]{};:'".,<>?«»“”„‘’]+$/
  };
  // http://www.iana.org/assignments/uri-schemes.html
  // http://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers#Well-known_ports
  URI.defaultPorts = {
    http: '80',
    https: '443',
    ftp: '21',
    gopher: '70',
    ws: '80',
    wss: '443'
  };
  // allowed hostname characters according to RFC 3986
  // ALPHA DIGIT "-" "." "_" "~" "!" "$" "&" "'" "(" ")" "*" "+" "," ";" "=" %encoded
  // I've never seen a (non-IDN) hostname other than: ALPHA DIGIT . -
  URI.invalid_hostname_characters = /[^a-zA-Z0-9\.-]/;
  // map DOM Elements to their URI attribute
  URI.domAttributes = {
    'a': 'href',
    'blockquote': 'cite',
    'link': 'href',
    'base': 'href',
    'script': 'src',
    'form': 'action',
    'img': 'src',
    'area': 'href',
    'iframe': 'src',
    'embed': 'src',
    'source': 'src',
    'track': 'src',
    'input': 'src' // but only if type="image"
  };
  URI.getDomAttribute = function(node) {
    if (!node || !node.nodeName) {
      return undefined;
    }

    var nodeName = node.nodeName.toLowerCase();
    // <input> should only expose src for type="image"
    if (nodeName === 'input' && node.type !== 'image') {
      return undefined;
    }

    return URI.domAttributes[nodeName];
  };

  function escapeForDumbFirefox36(value) {
    // https://github.com/medialize/URI.js/issues/91
    return escape(value);
  }

  // encoding / decoding according to RFC3986
  function strictEncodeURIComponent(string) {
    // see https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/encodeURIComponent
    return encodeURIComponent(string)
      .replace(/[!'()*]/g, escapeForDumbFirefox36)
      .replace(/\*/g, '%2A');
  }
  URI.encode = strictEncodeURIComponent;
  URI.decode = decodeURIComponent;
  URI.iso8859 = function() {
    URI.encode = escape;
    URI.decode = unescape;
  };
  URI.unicode = function() {
    URI.encode = strictEncodeURIComponent;
    URI.decode = decodeURIComponent;
  };
  URI.characters = {
    pathname: {
      encode: {
        // RFC3986 2.1: For consistency, URI producers and normalizers should
        // use uppercase hexadecimal digits for all percent-encodings.
        expression: /%(24|26|2B|2C|3B|3D|3A|40)/ig,
        map: {
          // -._~!'()*
          '%24': '$',
          '%26': '&',
          '%2B': '+',
          '%2C': ',',
          '%3B': ';',
          '%3D': '=',
          '%3A': ':',
          '%40': '@'
        }
      },
      decode: {
        expression: /[\/\?#]/g,
        map: {
          '/': '%2F',
          '?': '%3F',
          '#': '%23'
        }
      }
    },
    reserved: {
      encode: {
        // RFC3986 2.1: For consistency, URI producers and normalizers should
        // use uppercase hexadecimal digits for all percent-encodings.
        expression: /%(21|23|24|26|27|28|29|2A|2B|2C|2F|3A|3B|3D|3F|40|5B|5D)/ig,
        map: {
          // gen-delims
          '%3A': ':',
          '%2F': '/',
          '%3F': '?',
          '%23': '#',
          '%5B': '[',
          '%5D': ']',
          '%40': '@',
          // sub-delims
          '%21': '!',
          '%24': '$',
          '%26': '&',
          '%27': '\'',
          '%28': '(',
          '%29': ')',
          '%2A': '*',
          '%2B': '+',
          '%2C': ',',
          '%3B': ';',
          '%3D': '='
        }
      }
    }
  };
  URI.encodeQuery = function(string, escapeQuerySpace) {
    var escaped = URI.encode(string + '');
    if (escapeQuerySpace === undefined) {
      escapeQuerySpace = URI.escapeQuerySpace;
    }

    return escapeQuerySpace ? escaped.replace(/%20/g, '+') : escaped;
  };
  URI.decodeQuery = function(string, escapeQuerySpace) {
    string += '';
    if (escapeQuerySpace === undefined) {
      escapeQuerySpace = URI.escapeQuerySpace;
    }

    try {
      return URI.decode(escapeQuerySpace ? string.replace(/\+/g, '%20') : string);
    } catch(e) {
      // we're not going to mess with weird encodings,
      // give up and return the undecoded original string
      // see https://github.com/medialize/URI.js/issues/87
      // see https://github.com/medialize/URI.js/issues/92
      return string;
    }
  };
  URI.recodePath = function(string) {
    var segments = (string + '').split('/');
    for (var i = 0, length = segments.length; i < length; i++) {
      segments[i] = URI.encodePathSegment(URI.decode(segments[i]));
    }

    return segments.join('/');
  };
  URI.decodePath = function(string) {
    var segments = (string + '').split('/');
    for (var i = 0, length = segments.length; i < length; i++) {
      segments[i] = URI.decodePathSegment(segments[i]);
    }

    return segments.join('/');
  };
  // generate encode/decode path functions
  var _parts = {'encode':'encode', 'decode':'decode'};
  var _part;
  var generateAccessor = function(_group, _part) {
    return function(string) {
      return URI[_part](string + '').replace(URI.characters[_group][_part].expression, function(c) {
        return URI.characters[_group][_part].map[c];
      });
    };
  };

  for (_part in _parts) {
    URI[_part + 'PathSegment'] = generateAccessor('pathname', _parts[_part]);
  }

  URI.encodeReserved = generateAccessor('reserved', 'encode');

  URI.parse = function(string, parts) {
    var pos;
    if (!parts) {
      parts = {};
    }
    // [protocol"://"[username[":"password]"@"]hostname[":"port]"/"?][path]["?"querystring]["#"fragment]

    // extract fragment
    pos = string.indexOf('#');
    if (pos > -1) {
      // escaping?
      parts.fragment = string.substring(pos + 1) || null;
      string = string.substring(0, pos);
    }

    // extract query
    pos = string.indexOf('?');
    if (pos > -1) {
      // escaping?
      parts.query = string.substring(pos + 1) || null;
      string = string.substring(0, pos);
    }

    // extract protocol
    if (string.substring(0, 2) === '//') {
      // relative-scheme
      parts.protocol = null;
      string = string.substring(2);
      // extract "user:pass@host:port"
      string = URI.parseAuthority(string, parts);
    } else {
      pos = string.indexOf(':');
      if (pos > -1) {
        parts.protocol = string.substring(0, pos) || null;
        if (parts.protocol && !parts.protocol.match(URI.protocol_expression)) {
          // : may be within the path
          parts.protocol = undefined;
        } else if (parts.protocol === 'file') {
          // the file scheme: does not contain an authority
          string = string.substring(pos + 3);
        } else if (string.substring(pos + 1, pos + 3) === '//') {
          string = string.substring(pos + 3);

          // extract "user:pass@host:port"
          string = URI.parseAuthority(string, parts);
        } else {
          string = string.substring(pos + 1);
          parts.urn = true;
        }
      }
    }

    // what's left must be the path
    parts.path = string;

    // and we're done
    return parts;
  };
  URI.parseHost = function(string, parts) {
    // extract host:port
    var pos = string.indexOf('/');
    var bracketPos;
    var t;

    if (pos === -1) {
      pos = string.length;
    }

    if (string.charAt(0) === '[') {
      // IPv6 host - http://tools.ietf.org/html/draft-ietf-6man-text-addr-representation-04#section-6
      // I claim most client software breaks on IPv6 anyways. To simplify things, URI only accepts
      // IPv6+port in the format [2001:db8::1]:80 (for the time being)
      bracketPos = string.indexOf(']');
      parts.hostname = string.substring(1, bracketPos) || null;
      parts.port = string.substring(bracketPos + 2, pos) || null;
      if (parts.port === '/') {
        parts.port = null;
      }
    } else if (string.indexOf(':') !== string.lastIndexOf(':')) {
      // IPv6 host contains multiple colons - but no port
      // this notation is actually not allowed by RFC 3986, but we're a liberal parser
      parts.hostname = string.substring(0, pos) || null;
      parts.port = null;
    } else {
      t = string.substring(0, pos).split(':');
      parts.hostname = t[0] || null;
      parts.port = t[1] || null;
    }

    if (parts.hostname && string.substring(pos).charAt(0) !== '/') {
      pos++;
      string = '/' + string;
    }

    return string.substring(pos) || '/';
  };
  URI.parseAuthority = function(string, parts) {
    string = URI.parseUserinfo(string, parts);
    return URI.parseHost(string, parts);
  };
  URI.parseUserinfo = function(string, parts) {
    // extract username:password
    var firstSlash = string.indexOf('/');
    /*jshint laxbreak: true */
    var pos = firstSlash > -1
      ? string.lastIndexOf('@', firstSlash)
      : string.indexOf('@');
    /*jshint laxbreak: false */
    var t;

    // authority@ must come before /path
    if (pos > -1 && (firstSlash === -1 || pos < firstSlash)) {
      t = string.substring(0, pos).split(':');
      parts.username = t[0] ? URI.decode(t[0]) : null;
      t.shift();
      parts.password = t[0] ? URI.decode(t.join(':')) : null;
      string = string.substring(pos + 1);
    } else {
      parts.username = null;
      parts.password = null;
    }

    return string;
  };
  URI.parseQuery = function(string, escapeQuerySpace) {
    if (!string) {
      return {};
    }

    // throw out the funky business - "?"[name"="value"&"]+
    string = string.replace(/&+/g, '&').replace(/^\?*&*|&+$/g, '');

    if (!string) {
      return {};
    }

    var items = {};
    var splits = string.split('&');
    var length = splits.length;
    var v, name, value;

    for (var i = 0; i < length; i++) {
      v = splits[i].split('=');
      name = URI.decodeQuery(v.shift(), escapeQuerySpace);
      // no "=" is null according to http://dvcs.w3.org/hg/url/raw-file/tip/Overview.html#collect-url-parameters
      value = v.length ? URI.decodeQuery(v.join('='), escapeQuerySpace) : null;

      if (items[name]) {
        if (typeof items[name] === 'string') {
          items[name] = [items[name]];
        }

        items[name].push(value);
      } else {
        items[name] = value;
      }
    }

    return items;
  };

  URI.build = function(parts) {
    var t = '';

    if (parts.protocol) {
      t += parts.protocol + ':';
    }

    if (!parts.urn && (t || parts.hostname)) {
      t += '//';
    }

    t += (URI.buildAuthority(parts) || '');

    if (typeof parts.path === 'string') {
      if (parts.path.charAt(0) !== '/' && typeof parts.hostname === 'string') {
        t += '/';
      }

      t += parts.path;
    }

    if (typeof parts.query === 'string' && parts.query) {
      t += '?' + parts.query;
    }

    if (typeof parts.fragment === 'string' && parts.fragment) {
      t += '#' + parts.fragment;
    }
    return t;
  };
  URI.buildHost = function(parts) {
    var t = '';

    if (!parts.hostname) {
      return '';
    } else if (URI.ip6_expression.test(parts.hostname)) {
      t += '[' + parts.hostname + ']';
    } else {
      t += parts.hostname;
    }

    if (parts.port) {
      t += ':' + parts.port;
    }

    return t;
  };
  URI.buildAuthority = function(parts) {
    return URI.buildUserinfo(parts) + URI.buildHost(parts);
  };
  URI.buildUserinfo = function(parts) {
    var t = '';

    if (parts.username) {
      t += URI.encode(parts.username);

      if (parts.password) {
        t += ':' + URI.encode(parts.password);
      }

      t += '@';
    }

    return t;
  };
  URI.buildQuery = function(data, duplicateQueryParameters, escapeQuerySpace) {
    // according to http://tools.ietf.org/html/rfc3986 or http://labs.apache.org/webarch/uri/rfc/rfc3986.html
    // being »-._~!$&'()*+,;=:@/?« %HEX and alnum are allowed
    // the RFC explicitly states ?/foo being a valid use case, no mention of parameter syntax!
    // URI.js treats the query string as being application/x-www-form-urlencoded
    // see http://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type

    var t = '';
    var unique, key, i, length;
    for (key in data) {
      if (hasOwn.call(data, key) && key) {
        if (isArray(data[key])) {
          unique = {};
          for (i = 0, length = data[key].length; i < length; i++) {
            if (data[key][i] !== undefined && unique[data[key][i] + ''] === undefined) {
              t += '&' + URI.buildQueryParameter(key, data[key][i], escapeQuerySpace);
              if (duplicateQueryParameters !== true) {
                unique[data[key][i] + ''] = true;
              }
            }
          }
        } else if (data[key] !== undefined) {
          t += '&' + URI.buildQueryParameter(key, data[key], escapeQuerySpace);
        }
      }
    }

    return t.substring(1);
  };
  URI.buildQueryParameter = function(name, value, escapeQuerySpace) {
    // http://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type -- application/x-www-form-urlencoded
    // don't append "=" for null values, according to http://dvcs.w3.org/hg/url/raw-file/tip/Overview.html#url-parameter-serialization
    return URI.encodeQuery(name, escapeQuerySpace) + (value !== null ? '=' + URI.encodeQuery(value, escapeQuerySpace) : '');
  };

  URI.addQuery = function(data, name, value) {
    if (typeof name === 'object') {
      for (var key in name) {
        if (hasOwn.call(name, key)) {
          URI.addQuery(data, key, name[key]);
        }
      }
    } else if (typeof name === 'string') {
      if (data[name] === undefined) {
        data[name] = value;
        return;
      } else if (typeof data[name] === 'string') {
        data[name] = [data[name]];
      }

      if (!isArray(value)) {
        value = [value];
      }

      data[name] = data[name].concat(value);
    } else {
      throw new TypeError('URI.addQuery() accepts an object, string as the name parameter');
    }
  };
  URI.removeQuery = function(data, name, value) {
    var i, length, key;

    if (isArray(name)) {
      for (i = 0, length = name.length; i < length; i++) {
        data[name[i]] = undefined;
      }
    } else if (typeof name === 'object') {
      for (key in name) {
        if (hasOwn.call(name, key)) {
          URI.removeQuery(data, key, name[key]);
        }
      }
    } else if (typeof name === 'string') {
      if (value !== undefined) {
        if (data[name] === value) {
          data[name] = undefined;
        } else if (isArray(data[name])) {
          data[name] = filterArrayValues(data[name], value);
        }
      } else {
        data[name] = undefined;
      }
    } else {
      throw new TypeError('URI.addQuery() accepts an object, string as the first parameter');
    }
  };
  URI.hasQuery = function(data, name, value, withinArray) {
    if (typeof name === 'object') {
      for (var key in name) {
        if (hasOwn.call(name, key)) {
          if (!URI.hasQuery(data, key, name[key])) {
            return false;
          }
        }
      }

      return true;
    } else if (typeof name !== 'string') {
      throw new TypeError('URI.hasQuery() accepts an object, string as the name parameter');
    }

    switch (getType(value)) {
      case 'Undefined':
        // true if exists (but may be empty)
        return name in data; // data[name] !== undefined;

      case 'Boolean':
        // true if exists and non-empty
        var _booly = Boolean(isArray(data[name]) ? data[name].length : data[name]);
        return value === _booly;

      case 'Function':
        // allow complex comparison
        return !!value(data[name], name, data);

      case 'Array':
        if (!isArray(data[name])) {
          return false;
        }

        var op = withinArray ? arrayContains : arraysEqual;
        return op(data[name], value);

      case 'RegExp':
        if (!isArray(data[name])) {
          return Boolean(data[name] && data[name].match(value));
        }

        if (!withinArray) {
          return false;
        }

        return arrayContains(data[name], value);

      case 'Number':
        value = String(value);
        /* falls through */
      case 'String':
        if (!isArray(data[name])) {
          return data[name] === value;
        }

        if (!withinArray) {
          return false;
        }

        return arrayContains(data[name], value);

      default:
        throw new TypeError('URI.hasQuery() accepts undefined, boolean, string, number, RegExp, Function as the value parameter');
    }
  };


  URI.commonPath = function(one, two) {
    var length = Math.min(one.length, two.length);
    var pos;

    // find first non-matching character
    for (pos = 0; pos < length; pos++) {
      if (one.charAt(pos) !== two.charAt(pos)) {
        pos--;
        break;
      }
    }

    if (pos < 1) {
      return one.charAt(0) === two.charAt(0) && one.charAt(0) === '/' ? '/' : '';
    }

    // revert to last /
    if (one.charAt(pos) !== '/' || two.charAt(pos) !== '/') {
      pos = one.substring(0, pos).lastIndexOf('/');
    }

    return one.substring(0, pos + 1);
  };

  URI.withinString = function(string, callback, options) {
    options || (options = {});
    var _start = options.start || URI.findUri.start;
    var _end = options.end || URI.findUri.end;
    var _trim = options.trim || URI.findUri.trim;
    var _attributeOpen = /[a-z0-9-]=["']?$/i;

    _start.lastIndex = 0;
    while (true) {
      var match = _start.exec(string);
      if (!match) {
        break;
      }

      var start = match.index;
      if (options.ignoreHtml) {
        // attribut(e=["']?$)
        var attributeOpen = string.slice(Math.max(start - 3, 0), start);
        if (attributeOpen && _attributeOpen.test(attributeOpen)) {
          continue;
        }
      }

      var end = start + string.slice(start).search(_end);
      var slice = string.slice(start, end).replace(_trim, '');
      if (options.ignore && options.ignore.test(slice)) {
        continue;
      }

      end = start + slice.length;
      var result = callback(slice, start, end, string);
      string = string.slice(0, start) + result + string.slice(end);
      _start.lastIndex = start + result.length;
    }

    _start.lastIndex = 0;
    return string;
  };

  URI.ensureValidHostname = function(v) {
    // Theoretically URIs allow percent-encoding in Hostnames (according to RFC 3986)
    // they are not part of DNS and therefore ignored by URI.js

    if (v.match(URI.invalid_hostname_characters)) {
      // test punycode
      if (!punycode) {
        throw new TypeError('Hostname "' + v + '" contains characters other than [A-Z0-9.-] and Punycode.js is not available');
      }

      if (punycode.toASCII(v).match(URI.invalid_hostname_characters)) {
        throw new TypeError('Hostname "' + v + '" contains characters other than [A-Z0-9.-]');
      }
    }
  };

  // noConflict
  URI.noConflict = function(removeAll) {
    if (removeAll) {
      var unconflicted = {
        URI: this.noConflict()
      };

      if (root.URITemplate && typeof root.URITemplate.noConflict === 'function') {
        unconflicted.URITemplate = root.URITemplate.noConflict();
      }

      if (root.IPv6 && typeof root.IPv6.noConflict === 'function') {
        unconflicted.IPv6 = root.IPv6.noConflict();
      }

      if (root.SecondLevelDomains && typeof root.SecondLevelDomains.noConflict === 'function') {
        unconflicted.SecondLevelDomains = root.SecondLevelDomains.noConflict();
      }

      return unconflicted;
    } else if (root.URI === this) {
      root.URI = _URI;
    }

    return this;
  };

  p.build = function(deferBuild) {
    if (deferBuild === true) {
      this._deferred_build = true;
    } else if (deferBuild === undefined || this._deferred_build) {
      this._string = URI.build(this._parts);
      this._deferred_build = false;
    }

    return this;
  };

  p.clone = function() {
    return new URI(this);
  };

  p.valueOf = p.toString = function() {
    return this.build(false)._string;
  };

  // generate simple accessors
  _parts = {protocol: 'protocol', username: 'username', password: 'password', hostname: 'hostname',  port: 'port'};
  generateAccessor = function(_part){
    return function(v, build) {
      if (v === undefined) {
        return this._parts[_part] || '';
      } else {
        this._parts[_part] = v || null;
        this.build(!build);
        return this;
      }
    };
  };

  for (_part in _parts) {
    p[_part] = generateAccessor(_parts[_part]);
  }

  // generate accessors with optionally prefixed input
  _parts = {query: '?', fragment: '#'};
  generateAccessor = function(_part, _key){
    return function(v, build) {
      if (v === undefined) {
        return this._parts[_part] || '';
      } else {
        if (v !== null) {
          v = v + '';
          if (v.charAt(0) === _key) {
            v = v.substring(1);
          }
        }

        this._parts[_part] = v;
        this.build(!build);
        return this;
      }
    };
  };

  for (_part in _parts) {
    p[_part] = generateAccessor(_part, _parts[_part]);
  }

  // generate accessors with prefixed output
  _parts = {search: ['?', 'query'], hash: ['#', 'fragment']};
  generateAccessor = function(_part, _key){
    return function(v, build) {
      var t = this[_part](v, build);
      return typeof t === 'string' && t.length ? (_key + t) : t;
    };
  };

  for (_part in _parts) {
    p[_part] = generateAccessor(_parts[_part][1], _parts[_part][0]);
  }

  p.pathname = function(v, build) {
    if (v === undefined || v === true) {
      var res = this._parts.path || (this._parts.hostname ? '/' : '');
      return v ? URI.decodePath(res) : res;
    } else {
      this._parts.path = v ? URI.recodePath(v) : '/';
      this.build(!build);
      return this;
    }
  };
  p.path = p.pathname;
  p.href = function(href, build) {
    var key;

    if (href === undefined) {
      return this.toString();
    }

    this._string = '';
    this._parts = URI._parts();

    var _URI = href instanceof URI;
    var _object = typeof href === 'object' && (href.hostname || href.path || href.pathname);
    if (href.nodeName) {
      var attribute = URI.getDomAttribute(href);
      href = href[attribute] || '';
      _object = false;
    }

    // window.location is reported to be an object, but it's not the sort
    // of object we're looking for:
    // * location.protocol ends with a colon
    // * location.query != object.search
    // * location.hash != object.fragment
    // simply serializing the unknown object should do the trick
    // (for location, not for everything...)
    if (!_URI && _object && href.pathname !== undefined) {
      href = href.toString();
    }

    if (typeof href === 'string') {
      this._parts = URI.parse(href, this._parts);
    } else if (_URI || _object) {
      var src = _URI ? href._parts : href;
      for (key in src) {
        if (hasOwn.call(this._parts, key)) {
          this._parts[key] = src[key];
        }
      }
    } else {
      throw new TypeError('invalid input');
    }

    this.build(!build);
    return this;
  };

  // identification accessors
  p.is = function(what) {
    var ip = false;
    var ip4 = false;
    var ip6 = false;
    var name = false;
    var sld = false;
    var idn = false;
    var punycode = false;
    var relative = !this._parts.urn;

    if (this._parts.hostname) {
      relative = false;
      ip4 = URI.ip4_expression.test(this._parts.hostname);
      ip6 = URI.ip6_expression.test(this._parts.hostname);
      ip = ip4 || ip6;
      name = !ip;
      sld = name && SLD && SLD.has(this._parts.hostname);
      idn = name && URI.idn_expression.test(this._parts.hostname);
      punycode = name && URI.punycode_expression.test(this._parts.hostname);
    }

    switch (what.toLowerCase()) {
      case 'relative':
        return relative;

      case 'absolute':
        return !relative;

      // hostname identification
      case 'domain':
      case 'name':
        return name;

      case 'sld':
        return sld;

      case 'ip':
        return ip;

      case 'ip4':
      case 'ipv4':
      case 'inet4':
        return ip4;

      case 'ip6':
      case 'ipv6':
      case 'inet6':
        return ip6;

      case 'idn':
        return idn;

      case 'url':
        return !this._parts.urn;

      case 'urn':
        return !!this._parts.urn;

      case 'punycode':
        return punycode;
    }

    return null;
  };

  // component specific input validation
  var _protocol = p.protocol;
  var _port = p.port;
  var _hostname = p.hostname;

  p.protocol = function(v, build) {
    if (v !== undefined) {
      if (v) {
        // accept trailing ://
        v = v.replace(/:(\/\/)?$/, '');

        if (!v.match(URI.protocol_expression)) {
          throw new TypeError('Protocol "' + v + '" contains characters other than [A-Z0-9.+-] or doesn\'t start with [A-Z]');
        }
      }
    }
    return _protocol.call(this, v, build);
  };
  p.scheme = p.protocol;
  p.port = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v !== undefined) {
      if (v === 0) {
        v = null;
      }

      if (v) {
        v += '';
        if (v.charAt(0) === ':') {
          v = v.substring(1);
        }

        if (v.match(/[^0-9]/)) {
          throw new TypeError('Port "' + v + '" contains characters other than [0-9]');
        }
      }
    }
    return _port.call(this, v, build);
  };
  p.hostname = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v !== undefined) {
      var x = {};
      URI.parseHost(v, x);
      v = x.hostname;
    }
    return _hostname.call(this, v, build);
  };

  // compound accessors
  p.host = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v === undefined) {
      return this._parts.hostname ? URI.buildHost(this._parts) : '';
    } else {
      URI.parseHost(v, this._parts);
      this.build(!build);
      return this;
    }
  };
  p.authority = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v === undefined) {
      return this._parts.hostname ? URI.buildAuthority(this._parts) : '';
    } else {
      URI.parseAuthority(v, this._parts);
      this.build(!build);
      return this;
    }
  };
  p.userinfo = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v === undefined) {
      if (!this._parts.username) {
        return '';
      }

      var t = URI.buildUserinfo(this._parts);
      return t.substring(0, t.length -1);
    } else {
      if (v[v.length-1] !== '@') {
        v += '@';
      }

      URI.parseUserinfo(v, this._parts);
      this.build(!build);
      return this;
    }
  };
  p.resource = function(v, build) {
    var parts;

    if (v === undefined) {
      return this.path() + this.search() + this.hash();
    }

    parts = URI.parse(v);
    this._parts.path = parts.path;
    this._parts.query = parts.query;
    this._parts.fragment = parts.fragment;
    this.build(!build);
    return this;
  };

  // fraction accessors
  p.subdomain = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    // convenience, return "www" from "www.example.org"
    if (v === undefined) {
      if (!this._parts.hostname || this.is('IP')) {
        return '';
      }

      // grab domain and add another segment
      var end = this._parts.hostname.length - this.domain().length - 1;
      return this._parts.hostname.substring(0, end) || '';
    } else {
      var e = this._parts.hostname.length - this.domain().length;
      var sub = this._parts.hostname.substring(0, e);
      var replace = new RegExp('^' + escapeRegEx(sub));

      if (v && v.charAt(v.length - 1) !== '.') {
        v += '.';
      }

      if (v) {
        URI.ensureValidHostname(v);
      }

      this._parts.hostname = this._parts.hostname.replace(replace, v);
      this.build(!build);
      return this;
    }
  };
  p.domain = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (typeof v === 'boolean') {
      build = v;
      v = undefined;
    }

    // convenience, return "example.org" from "www.example.org"
    if (v === undefined) {
      if (!this._parts.hostname || this.is('IP')) {
        return '';
      }

      // if hostname consists of 1 or 2 segments, it must be the domain
      var t = this._parts.hostname.match(/\./g);
      if (t && t.length < 2) {
        return this._parts.hostname;
      }

      // grab tld and add another segment
      var end = this._parts.hostname.length - this.tld(build).length - 1;
      end = this._parts.hostname.lastIndexOf('.', end -1) + 1;
      return this._parts.hostname.substring(end) || '';
    } else {
      if (!v) {
        throw new TypeError('cannot set domain empty');
      }

      URI.ensureValidHostname(v);

      if (!this._parts.hostname || this.is('IP')) {
        this._parts.hostname = v;
      } else {
        var replace = new RegExp(escapeRegEx(this.domain()) + '$');
        this._parts.hostname = this._parts.hostname.replace(replace, v);
      }

      this.build(!build);
      return this;
    }
  };
  p.tld = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (typeof v === 'boolean') {
      build = v;
      v = undefined;
    }

    // return "org" from "www.example.org"
    if (v === undefined) {
      if (!this._parts.hostname || this.is('IP')) {
        return '';
      }

      var pos = this._parts.hostname.lastIndexOf('.');
      var tld = this._parts.hostname.substring(pos + 1);

      if (build !== true && SLD && SLD.list[tld.toLowerCase()]) {
        return SLD.get(this._parts.hostname) || tld;
      }

      return tld;
    } else {
      var replace;

      if (!v) {
        throw new TypeError('cannot set TLD empty');
      } else if (v.match(/[^a-zA-Z0-9-]/)) {
        if (SLD && SLD.is(v)) {
          replace = new RegExp(escapeRegEx(this.tld()) + '$');
          this._parts.hostname = this._parts.hostname.replace(replace, v);
        } else {
          throw new TypeError('TLD "' + v + '" contains characters other than [A-Z0-9]');
        }
      } else if (!this._parts.hostname || this.is('IP')) {
        throw new ReferenceError('cannot set TLD on non-domain host');
      } else {
        replace = new RegExp(escapeRegEx(this.tld()) + '$');
        this._parts.hostname = this._parts.hostname.replace(replace, v);
      }

      this.build(!build);
      return this;
    }
  };
  p.directory = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v === undefined || v === true) {
      if (!this._parts.path && !this._parts.hostname) {
        return '';
      }

      if (this._parts.path === '/') {
        return '/';
      }

      var end = this._parts.path.length - this.filename().length - 1;
      var res = this._parts.path.substring(0, end) || (this._parts.hostname ? '/' : '');

      return v ? URI.decodePath(res) : res;

    } else {
      var e = this._parts.path.length - this.filename().length;
      var directory = this._parts.path.substring(0, e);
      var replace = new RegExp('^' + escapeRegEx(directory));

      // fully qualifier directories begin with a slash
      if (!this.is('relative')) {
        if (!v) {
          v = '/';
        }

        if (v.charAt(0) !== '/') {
          v = '/' + v;
        }
      }

      // directories always end with a slash
      if (v && v.charAt(v.length - 1) !== '/') {
        v += '/';
      }

      v = URI.recodePath(v);
      this._parts.path = this._parts.path.replace(replace, v);
      this.build(!build);
      return this;
    }
  };
  p.filename = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v === undefined || v === true) {
      if (!this._parts.path || this._parts.path === '/') {
        return '';
      }

      var pos = this._parts.path.lastIndexOf('/');
      var res = this._parts.path.substring(pos+1);

      return v ? URI.decodePathSegment(res) : res;
    } else {
      var mutatedDirectory = false;

      if (v.charAt(0) === '/') {
        v = v.substring(1);
      }

      if (v.match(/\.?\//)) {
        mutatedDirectory = true;
      }

      var replace = new RegExp(escapeRegEx(this.filename()) + '$');
      v = URI.recodePath(v);
      this._parts.path = this._parts.path.replace(replace, v);

      if (mutatedDirectory) {
        this.normalizePath(build);
      } else {
        this.build(!build);
      }

      return this;
    }
  };
  p.suffix = function(v, build) {
    if (this._parts.urn) {
      return v === undefined ? '' : this;
    }

    if (v === undefined || v === true) {
      if (!this._parts.path || this._parts.path === '/') {
        return '';
      }

      var filename = this.filename();
      var pos = filename.lastIndexOf('.');
      var s, res;

      if (pos === -1) {
        return '';
      }

      // suffix may only contain alnum characters (yup, I made this up.)
      s = filename.substring(pos+1);
      res = (/^[a-z0-9%]+$/i).test(s) ? s : '';
      return v ? URI.decodePathSegment(res) : res;
    } else {
      if (v.charAt(0) === '.') {
        v = v.substring(1);
      }

      var suffix = this.suffix();
      var replace;

      if (!suffix) {
        if (!v) {
          return this;
        }

        this._parts.path += '.' + URI.recodePath(v);
      } else if (!v) {
        replace = new RegExp(escapeRegEx('.' + suffix) + '$');
      } else {
        replace = new RegExp(escapeRegEx(suffix) + '$');
      }

      if (replace) {
        v = URI.recodePath(v);
        this._parts.path = this._parts.path.replace(replace, v);
      }

      this.build(!build);
      return this;
    }
  };
  p.segment = function(segment, v, build) {
    var separator = this._parts.urn ? ':' : '/';
    var path = this.path();
    var absolute = path.substring(0, 1) === '/';
    var segments = path.split(separator);

    if (segment !== undefined && typeof segment !== 'number') {
      build = v;
      v = segment;
      segment = undefined;
    }

    if (segment !== undefined && typeof segment !== 'number') {
      throw new Error('Bad segment "' + segment + '", must be 0-based integer');
    }

    if (absolute) {
      segments.shift();
    }

    if (segment < 0) {
      // allow negative indexes to address from the end
      segment = Math.max(segments.length + segment, 0);
    }

    if (v === undefined) {
      /*jshint laxbreak: true */
      return segment === undefined
        ? segments
        : segments[segment];
      /*jshint laxbreak: false */
    } else if (segment === null || segments[segment] === undefined) {
      if (isArray(v)) {
        segments = [];
        // collapse empty elements within array
        for (var i=0, l=v.length; i < l; i++) {
          if (!v[i].length && (!segments.length || !segments[segments.length -1].length)) {
            continue;
          }

          if (segments.length && !segments[segments.length -1].length) {
            segments.pop();
          }

          segments.push(v[i]);
        }
      } else if (v || (typeof v === 'string')) {
        if (segments[segments.length -1] === '') {
          // empty trailing elements have to be overwritten
          // to prevent results such as /foo//bar
          segments[segments.length -1] = v;
        } else {
          segments.push(v);
        }
      }
    } else {
      if (v || (typeof v === 'string' && v.length)) {
        segments[segment] = v;
      } else {
        segments.splice(segment, 1);
      }
    }

    if (absolute) {
      segments.unshift('');
    }

    return this.path(segments.join(separator), build);
  };
  p.segmentCoded = function(segment, v, build) {
    var segments, i, l;

    if (typeof segment !== 'number') {
      build = v;
      v = segment;
      segment = undefined;
    }

    if (v === undefined) {
      segments = this.segment(segment, v, build);
      if (!isArray(segments)) {
        segments = segments !== undefined ? URI.decode(segments) : undefined;
      } else {
        for (i = 0, l = segments.length; i < l; i++) {
          segments[i] = URI.decode(segments[i]);
        }
      }

      return segments;
    }

    if (!isArray(v)) {
      v = typeof v === 'string' ? URI.encode(v) : v;
    } else {
      for (i = 0, l = v.length; i < l; i++) {
        v[i] = URI.decode(v[i]);
      }
    }

    return this.segment(segment, v, build);
  };

  // mutating query string
  var q = p.query;
  p.query = function(v, build) {
    if (v === true) {
      return URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace);
    } else if (typeof v === 'function') {
      var data = URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace);
      var result = v.call(this, data);
      this._parts.query = URI.buildQuery(result || data, this._parts.duplicateQueryParameters, this._parts.escapeQuerySpace);
      this.build(!build);
      return this;
    } else if (v !== undefined && typeof v !== 'string') {
      this._parts.query = URI.buildQuery(v, this._parts.duplicateQueryParameters, this._parts.escapeQuerySpace);
      this.build(!build);
      return this;
    } else {
      return q.call(this, v, build);
    }
  };
  p.setQuery = function(name, value, build) {
    var data = URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace);

    if (typeof name === 'object') {
      for (var key in name) {
        if (hasOwn.call(name, key)) {
          data[key] = name[key];
        }
      }
    } else if (typeof name === 'string') {
      data[name] = value !== undefined ? value : null;
    } else {
      throw new TypeError('URI.addQuery() accepts an object, string as the name parameter');
    }

    this._parts.query = URI.buildQuery(data, this._parts.duplicateQueryParameters, this._parts.escapeQuerySpace);
    if (typeof name !== 'string') {
      build = value;
    }

    this.build(!build);
    return this;
  };
  p.addQuery = function(name, value, build) {
    var data = URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace);
    URI.addQuery(data, name, value === undefined ? null : value);
    this._parts.query = URI.buildQuery(data, this._parts.duplicateQueryParameters, this._parts.escapeQuerySpace);
    if (typeof name !== 'string') {
      build = value;
    }

    this.build(!build);
    return this;
  };
  p.removeQuery = function(name, value, build) {
    var data = URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace);
    URI.removeQuery(data, name, value);
    this._parts.query = URI.buildQuery(data, this._parts.duplicateQueryParameters, this._parts.escapeQuerySpace);
    if (typeof name !== 'string') {
      build = value;
    }

    this.build(!build);
    return this;
  };
  p.hasQuery = function(name, value, withinArray) {
    var data = URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace);
    return URI.hasQuery(data, name, value, withinArray);
  };
  p.setSearch = p.setQuery;
  p.addSearch = p.addQuery;
  p.removeSearch = p.removeQuery;
  p.hasSearch = p.hasQuery;

  // sanitizing URLs
  p.normalize = function() {
    if (this._parts.urn) {
      return this
        .normalizeProtocol(false)
        .normalizeQuery(false)
        .normalizeFragment(false)
        .build();
    }

    return this
      .normalizeProtocol(false)
      .normalizeHostname(false)
      .normalizePort(false)
      .normalizePath(false)
      .normalizeQuery(false)
      .normalizeFragment(false)
      .build();
  };
  p.normalizeProtocol = function(build) {
    if (typeof this._parts.protocol === 'string') {
      this._parts.protocol = this._parts.protocol.toLowerCase();
      this.build(!build);
    }

    return this;
  };
  p.normalizeHostname = function(build) {
    if (this._parts.hostname) {
      if (this.is('IDN') && punycode) {
        this._parts.hostname = punycode.toASCII(this._parts.hostname);
      } else if (this.is('IPv6') && IPv6) {
        this._parts.hostname = IPv6.best(this._parts.hostname);
      }

      this._parts.hostname = this._parts.hostname.toLowerCase();
      this.build(!build);
    }

    return this;
  };
  p.normalizePort = function(build) {
    // remove port of it's the protocol's default
    if (typeof this._parts.protocol === 'string' && this._parts.port === URI.defaultPorts[this._parts.protocol]) {
      this._parts.port = null;
      this.build(!build);
    }

    return this;
  };
  p.normalizePath = function(build) {
    if (this._parts.urn) {
      return this;
    }

    if (!this._parts.path || this._parts.path === '/') {
      return this;
    }

    var _was_relative;
    var _path = this._parts.path;
    var _leadingParents = '';
    var _parent, _pos;

    // handle relative paths
    if (_path.charAt(0) !== '/') {
      _was_relative = true;
      _path = '/' + _path;
    }

    // resolve simples
    _path = _path
      .replace(/(\/(\.\/)+)|(\/\.$)/g, '/')
      .replace(/\/{2,}/g, '/');

    // remember leading parents
    if (_was_relative) {
      _leadingParents = _path.substring(1).match(/^(\.\.\/)+/) || '';
      if (_leadingParents) {
        _leadingParents = _leadingParents[0];
      }
    }

    // resolve parents
    while (true) {
      _parent = _path.indexOf('/..');
      if (_parent === -1) {
        // no more ../ to resolve
        break;
      } else if (_parent === 0) {
        // top level cannot be relative, skip it
        _path = _path.substring(3);
        continue;
      }

      _pos = _path.substring(0, _parent).lastIndexOf('/');
      if (_pos === -1) {
        _pos = _parent;
      }
      _path = _path.substring(0, _pos) + _path.substring(_parent + 3);
    }

    // revert to relative
    if (_was_relative && this.is('relative')) {
      _path = _leadingParents + _path.substring(1);
    }

    _path = URI.recodePath(_path);
    this._parts.path = _path;
    this.build(!build);
    return this;
  };
  p.normalizePathname = p.normalizePath;
  p.normalizeQuery = function(build) {
    if (typeof this._parts.query === 'string') {
      if (!this._parts.query.length) {
        this._parts.query = null;
      } else {
        this.query(URI.parseQuery(this._parts.query, this._parts.escapeQuerySpace));
      }

      this.build(!build);
    }

    return this;
  };
  p.normalizeFragment = function(build) {
    if (!this._parts.fragment) {
      this._parts.fragment = null;
      this.build(!build);
    }

    return this;
  };
  p.normalizeSearch = p.normalizeQuery;
  p.normalizeHash = p.normalizeFragment;

  p.iso8859 = function() {
    // expect unicode input, iso8859 output
    var e = URI.encode;
    var d = URI.decode;

    URI.encode = escape;
    URI.decode = decodeURIComponent;
    this.normalize();
    URI.encode = e;
    URI.decode = d;
    return this;
  };

  p.unicode = function() {
    // expect iso8859 input, unicode output
    var e = URI.encode;
    var d = URI.decode;

    URI.encode = strictEncodeURIComponent;
    URI.decode = unescape;
    this.normalize();
    URI.encode = e;
    URI.decode = d;
    return this;
  };

  p.readable = function() {
    var uri = this.clone();
    // removing username, password, because they shouldn't be displayed according to RFC 3986
    uri.username('').password('').normalize();
    var t = '';
    if (uri._parts.protocol) {
      t += uri._parts.protocol + '://';
    }

    if (uri._parts.hostname) {
      if (uri.is('punycode') && punycode) {
        t += punycode.toUnicode(uri._parts.hostname);
        if (uri._parts.port) {
          t += ':' + uri._parts.port;
        }
      } else {
        t += uri.host();
      }
    }

    if (uri._parts.hostname && uri._parts.path && uri._parts.path.charAt(0) !== '/') {
      t += '/';
    }

    t += uri.path(true);
    if (uri._parts.query) {
      var q = '';
      for (var i = 0, qp = uri._parts.query.split('&'), l = qp.length; i < l; i++) {
        var kv = (qp[i] || '').split('=');
        q += '&' + URI.decodeQuery(kv[0], this._parts.escapeQuerySpace)
          .replace(/&/g, '%26');

        if (kv[1] !== undefined) {
          q += '=' + URI.decodeQuery(kv[1], this._parts.escapeQuerySpace)
            .replace(/&/g, '%26');
        }
      }
      t += '?' + q.substring(1);
    }

    t += URI.decodeQuery(uri.hash(), true);
    return t;
  };

  // resolving relative and absolute URLs
  p.absoluteTo = function(base) {
    var resolved = this.clone();
    var properties = ['protocol', 'username', 'password', 'hostname', 'port'];
    var basedir, i, p;

    if (this._parts.urn) {
      throw new Error('URNs do not have any generally defined hierarchical components');
    }

    if (!(base instanceof URI)) {
      base = new URI(base);
    }

    if (!resolved._parts.protocol) {
      resolved._parts.protocol = base._parts.protocol;
    }

    if (this._parts.hostname) {
      return resolved;
    }

    for (i = 0; (p = properties[i]); i++) {
      resolved._parts[p] = base._parts[p];
    }

    if (!resolved._parts.path) {
      resolved._parts.path = base._parts.path;
      if (!resolved._parts.query) {
        resolved._parts.query = base._parts.query;
      }
    } else if (resolved._parts.path.substring(-2) === '..') {
      resolved._parts.path += '/';
    }

    if (resolved.path().charAt(0) !== '/') {
      basedir = base.directory();
      resolved._parts.path = (basedir ? (basedir + '/') : '') + resolved._parts.path;
      resolved.normalizePath();
    }

    resolved.build();
    return resolved;
  };
  p.relativeTo = function(base) {
    var relative = this.clone().normalize();
    var relativeParts, baseParts, common, relativePath, basePath;

    if (relative._parts.urn) {
      throw new Error('URNs do not have any generally defined hierarchical components');
    }

    base = new URI(base).normalize();
    relativeParts = relative._parts;
    baseParts = base._parts;
    relativePath = relative.path();
    basePath = base.path();

    if (relativePath.charAt(0) !== '/') {
      throw new Error('URI is already relative');
    }

    if (basePath.charAt(0) !== '/') {
      throw new Error('Cannot calculate a URI relative to another relative URI');
    }

    if (relativeParts.protocol === baseParts.protocol) {
      relativeParts.protocol = null;
    }

    if (relativeParts.username !== baseParts.username || relativeParts.password !== baseParts.password) {
      return relative.build();
    }

    if (relativeParts.protocol !== null || relativeParts.username !== null || relativeParts.password !== null) {
      return relative.build();
    }

    if (relativeParts.hostname === baseParts.hostname && relativeParts.port === baseParts.port) {
      relativeParts.hostname = null;
      relativeParts.port = null;
    } else {
      return relative.build();
    }

    if (relativePath === basePath) {
      relativeParts.path = '';
      return relative.build();
    }

    // determine common sub path
    common = URI.commonPath(relative.path(), base.path());

    // If the paths have nothing in common, return a relative URL with the absolute path.
    if (!common) {
      return relative.build();
    }

    var parents = baseParts.path
      .substring(common.length)
      .replace(/[^\/]*$/, '')
      .replace(/.*?\//g, '../');

    relativeParts.path = parents + relativeParts.path.substring(common.length);

    return relative.build();
  };

  // comparing URIs
  p.equals = function(uri) {
    var one = this.clone();
    var two = new URI(uri);
    var one_map = {};
    var two_map = {};
    var checked = {};
    var one_query, two_query, key;

    one.normalize();
    two.normalize();

    // exact match
    if (one.toString() === two.toString()) {
      return true;
    }

    // extract query string
    one_query = one.query();
    two_query = two.query();
    one.query('');
    two.query('');

    // definitely not equal if not even non-query parts match
    if (one.toString() !== two.toString()) {
      return false;
    }

    // query parameters have the same length, even if they're permuted
    if (one_query.length !== two_query.length) {
      return false;
    }

    one_map = URI.parseQuery(one_query, this._parts.escapeQuerySpace);
    two_map = URI.parseQuery(two_query, this._parts.escapeQuerySpace);

    for (key in one_map) {
      if (hasOwn.call(one_map, key)) {
        if (!isArray(one_map[key])) {
          if (one_map[key] !== two_map[key]) {
            return false;
          }
        } else if (!arraysEqual(one_map[key], two_map[key])) {
          return false;
        }

        checked[key] = true;
      }
    }

    for (key in two_map) {
      if (hasOwn.call(two_map, key)) {
        if (!checked[key]) {
          // two contains a parameter not present in one
          return false;
        }
      }
    }

    return true;
  };

  // state
  p.duplicateQueryParameters = function(v) {
    this._parts.duplicateQueryParameters = !!v;
    return this;
  };

  p.escapeQuerySpace = function(v) {
    this._parts.escapeQuerySpace = !!v;
    return this;
  };

  return URI;
}));

var __extends = this && this.__extends || function(d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() {
        this.constructor = d;
    }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

angular.module("nr-ng-client", []).run([ "nrNgClient", "nrNgUiRouter", function(nrNgClient, nrNgUiRouter) {} ]);

var relic;

(function(relic) {
    var NrNgClient = function() {
        function NrNgClient() {
            this.newrelic = window.newrelic;
        }
        NrNgClient.prototype.tag = function(name, value) {
            if (this.newrelic) {
                this.newrelic.setCustomAttribute(name, value);
            }
        };
        NrNgClient.prototype.addPageAction = function(name, values) {
            if (this.newrelic) {
                this.newrelic.addPageAction(name, values);
            }
        };
        NrNgClient.prototype.reportError = function(exception, cause) {
            if (this.newrelic) {
                try {
                    throw typeof exception === "string" ? new Error(exception) : exception;
                } catch (e) {
                    this.newrelic.noticeError(e);
                    if (typeof this.newrelic.addPageAction === "function") {
                        this.newrelic.addPageAction("error", {
                            href: window.location.href,
                            message: e.message,
                            stack: e.stack,
                            cause: cause || ""
                        });
                    }
                }
            }
        };
        NrNgClient.prototype.reportFinishLoading = function() {
            if (this.newrelic) {
                this.newrelic.finished();
            }
        };
        return NrNgClient;
    }();
    relic.NrNgClient = NrNgClient;
    var NrNgClientProvider = function(_super) {
        __extends(NrNgClientProvider, _super);
        function NrNgClientProvider() {
            _super.apply(this, arguments);
            this.config = {
                reportError: true,
                reportFinish: true
            };
        }
        NrNgClientProvider.prototype.reportFinishedEnabled = function(value) {
            this.config.reportFinish = value;
        };
        NrNgClientProvider.prototype.reportErrorsEnabled = function(value) {
            this.config.reportError = value;
        };
        NrNgClientProvider.prototype.decorateExceptionHandler = function($provide) {
            var _this = this;
            if (this.newrelic && this.config.reportError) {
                $provide.decorator("$exceptionHandler", [ "$delegate", function($delegate) {
                    return function(exception, cause) {
                        _this.reportError(exception, cause);
                        $delegate(exception, cause);
                    };
                } ]);
            }
        };
        NrNgClientProvider.prototype.$get = function($browser, $timeout) {
            var _this = this;
            if (this.config.reportFinish) {
                $timeout(function() {
                    $browser.notifyWhenNoOutstandingRequests(function() {
                        _this.reportFinishLoading();
                    });
                }, 0, false);
            }
            return new NrNgClient();
        };
        NrNgClientProvider.prototype.$get.$inject = [ "$browser", "$timeout" ];
        return NrNgClientProvider;
    }(NrNgClient);
    relic.NrNgClientProvider = NrNgClientProvider;
    angular.module("nr-ng-client").provider("nrNgClient", NrNgClientProvider).config([ "$provide", "nrNgClientProvider", function($provide, nrNgClientProvider) {
        nrNgClientProvider.decorateExceptionHandler($provide);
    } ]);
})(relic || (relic = {}));

var relic;

(function(relic) {
    var STATE_CHANGE_START_EVENT_NAME = "$stateChangeStart";
    var STATE_CHANGE_END_EVENT_NAME = "$viewContentLoaded";
    var NrNgUiRouter = function() {
        function NrNgUiRouter($state, addStateToTrace) {
            this.$state = $state;
            this.addStateToTrace = addStateToTrace;
        }
        NrNgUiRouter.prototype.reportStateDataLoaded = function() {
            if (this.$state) {
                this.addStateToTrace(this.$state.current.url);
            }
        };
        return NrNgUiRouter;
    }();
    relic.NrNgUiRouter = NrNgUiRouter;
    var NrNgUiRouterProvider = function() {
        function NrNgUiRouterProvider() {
            this.newrelic = window.newrelic;
            this.config = {
                reportStateChange: true,
                threshold: 300
            };
            this.routsData = {
                start: 0,
                inTransition: false
            };
        }
        NrNgUiRouterProvider.prototype.stateChangedReportEnabled = function(val) {
            this.config.reportStateChange = val;
        };
        NrNgUiRouterProvider.prototype.threshold = function(value) {
            this.config.threshold = value;
        };
        NrNgUiRouterProvider.prototype.registerStateChangeEvents = function($rootScope, $browser, $state) {
            var _this = this;
            $rootScope.$on(STATE_CHANGE_START_EVENT_NAME, function(ev, toState, toParams, fromState) {
                if (fromState && fromState.name) {
                    _this.routsData.start = Date.now();
                    _this.routsData.inTransition = true;
                }
            });
            if (this.config.reportStateChange) {
                $rootScope.$on(STATE_CHANGE_END_EVENT_NAME, function() {
                    if (_this.routsData.inTransition) {
                        $browser.notifyWhenNoOutstandingRequests(function() {
                            _this.addStateToTrace($state.current.url);
                        });
                    }
                });
            }
        };
        NrNgUiRouterProvider.prototype.addStateToTrace = function(name) {
            this.routsData.inTransition = false;
            if (this.newrelic && Date.now() - this.routsData.start >= this.config.threshold) {
                this.newrelic.addToTrace({
                    name: "RENDER_STATE" + name,
                    start: this.routsData.start,
                    end: Date.now()
                });
                this.newrelic.addPageAction("RENDER_STATE" + name, {
                    duration: (Date.now() - this.routsData.start) / 1e3
                });
            }
        };
        NrNgUiRouterProvider.prototype.getStateService = function($injector) {
            var $state;
            var routerService = "$state";
            try {
                var hasUiRouter = $injector.has(routerService);
                if (hasUiRouter) {
                    $state = $injector.get(routerService);
                }
            } catch (e) {}
            return $state;
        };
        NrNgUiRouterProvider.prototype.$get = function($rootScope, $browser, $injector) {
            var _this = this;
            var $state = this.getStateService($injector);
            if ($state) {
                this.registerStateChangeEvents($rootScope, $browser, $state);
            }
            return new NrNgUiRouter($state, function(url) {
                return _this.addStateToTrace(url);
            });
        };
        NrNgUiRouterProvider.prototype.$get.$inject = [ "$rootScope", "$browser", "$injector" ];
        return NrNgUiRouterProvider;
    }();
    relic.NrNgUiRouterProvider = NrNgUiRouterProvider;
    angular.module("nr-ng-client").provider("nrNgUiRouter", NrNgUiRouterProvider);
})(relic || (relic = {}));

"use strict";

if (angular.version.minor < 5) {
    var hooked = angular.module;
    angular.module = function() {
        var moduleInstance = hooked.apply(this, arguments);
        var CNTRL_REG = /^(\S+)(\s+as\s+(\w+))?$/;
        function identifierForController(controller) {
            if (angular.isString(controller)) {
                var match = CNTRL_REG.exec(controller);
                return match && match[3];
            }
        }
        if (!moduleInstance.component) {
            moduleInstance.component = function(name, options) {
                function factory($injector) {
                    function makeInjectable(fn) {
                        if (angular.isFunction(fn) || angular.isArray(fn)) {
                            return function(tElement, tAttrs) {
                                return $injector.invoke(fn, this, {
                                    $element: tElement,
                                    $attrs: tAttrs
                                });
                            };
                        } else {
                            return fn;
                        }
                    }
                    var template = !options.template && !options.templateUrl ? "" : options.template;
                    return {
                        controller: options.controller || function() {},
                        controllerAs: identifierForController(options.controller) || options.controllerAs || "$ctrl",
                        template: makeInjectable(template),
                        templateUrl: makeInjectable(options.templateUrl),
                        transclude: options.transclude,
                        scope: options.bindings || {},
                        bindToController: true,
                        restrict: options.restrict || "E"
                    };
                }
                factory.$inject = [ "$injector" ];
                return moduleInstance.directive(name, factory);
            };
        }
        return moduleInstance;
    };
}

(function() {
    var ngRoute;
    try {
        ngRoute = angular.module("ngRoute");
    } catch (e) {}
    if (ngRoute) {
        ngRoute.config([ "$routeProvider", function($routeProvider) {
            function fix(obj) {
                if (obj.resolve && !obj.controller) {
                    var route = {};
                    route.controllerAs = "$resolve";
                    route.controller = function() {
                        var _this = this;
                        var args = arguments;
                        Object.keys(obj.resolve).forEach(function(key, index) {
                            return _this[key] = args[index];
                        });
                    };
                    route.controller.$inject = Object.keys(obj.resolve);
                    return angular.extend(route, obj);
                } else {
                    return obj;
                }
            }
            var hooked = $routeProvider.when;
            $routeProvider.when = function() {
                arguments[1] = fix(arguments[1]);
                return hooked.apply(this, arguments);
            };
        } ]);
    }
})();

var PermissionsDefinition = function() {
    function PermissionsDefinition(json) {
        this.permissions = json.permissions;
        this.isOwner = json.isOwner;
        this.$$ownerId = json.ownerId;
        this.$$roles = json.roles;
        this.siteToken = json.siteToken || "user-unknown";
    }
    return PermissionsDefinition;
}();

var PermissionsDefinitionBuilder = function() {
    function PermissionsDefinitionBuilder() {
        this.roles = [];
        this.permissions = [];
        this.siteToken = "user-unknown";
    }
    PermissionsDefinitionBuilder.prototype.withPermissions = function(permissions) {
        this.permissions = permissions;
        return this;
    };
    PermissionsDefinitionBuilder.prototype.withIsOwner = function(isOwner) {
        this.isOwner = isOwner;
        return this;
    };
    PermissionsDefinitionBuilder.prototype.withOwnerId = function(ownerId) {
        this.ownerId = ownerId;
        return this;
    };
    PermissionsDefinitionBuilder.prototype.withSiteToken = function(token) {
        this.siteToken = token;
        return this;
    };
    PermissionsDefinitionBuilder.prototype.withRoles = function(roles) {
        this.roles = roles;
        return this;
    };
    PermissionsDefinitionBuilder.prototype.build = function() {
        return new PermissionsDefinition({
            permissions: this.permissions,
            isOwner: this.isOwner,
            roles: this.roles,
            ownerId: this.ownerId,
            siteToken: this.siteToken
        });
    };
    return PermissionsDefinitionBuilder;
}();

"use strict";

angular.module("wixAngularExperiments", []);

angular.module("wixAngularPermissionsConstants", []);

angular.module("wixAngularPermissions", [ "wixAngularPermissionsConstants" ]);

angular.module("wixAngularBackwardCompatibility", [ "wixAngularAppInternal", "wixAngularExperiments" ]);

angular.module("wixAngularStorage", [ "wixAngularAppInternal" ]);

angular.module("wixAngularStorageHub", [ "wixAngularStorage" ]);

angular.module("wixAngularAppInternal", [ "nr-ng-client" ]);

angular.module("wixAngularInterceptor", [ "wixAngularAppInternal" ]).config([ "$httpProvider", "wixAngularTopologyProvider", function($httpProvider, wixAngularTopologyProvider) {
    $httpProvider.interceptors.push("wixAngularInterceptor");
    if (!wixAngularTopologyProvider.getStaticsUrl()) {
        wixAngularTopologyProvider.setStaticsUrl(angular.element(document).find("base").attr("href"));
    }
} ]);

angular.module("wixAngularTranslateCompile", [ "pascalprecht.translate" ]);

angular.module("wixAngularConstants", []);

angular.module("wixAngular", [ "wixAngularAppInternal", "wixAngularTranslateCompile", "wixAngularStorage", "wixAngularExperiments", "wixAngularInterceptor", "wixAngularBackwardCompatibility", "wixAngularPermissions", "wixAngularConstants" ]);

"use strict";

angular.module("wixAngularStorage").constant("ANGULAR_STORAGE_PREFIX", "wixAngularStorage").constant("KEY_SEPARATOR", "|").constant("DEFAULT_AGE_IN_SEC", 60 * 60).constant("CLEANING_INTERVAL", 1e3 * 60 * 10).constant("CLEAN_EPSILON", 100).constant("MAX_KEY_LENGTH", 100).constant("MAX_VALUE_SIZE_IN_BYTES", 4 * 1024).constant("MAX_AGE_IN_SEC", 60 * 60 * 24 * 2).constant("MAX_STORAGE_SIZE_IN_BYTES", 1024 * 1024).constant("DATA_TYPE", "data").constant("ADHOC_TYPE", "adhoc").constant("REMOTE_TYPE", "remote").constant("wixAngularStorageErrors", {
    LOGGED_OUT: 1,
    NOT_FOUND: 2,
    RUNTIME_EXCEPTION: 3,
    SERVER_ERROR: 4,
    QUOTA_EXCEEDED: 5
});

angular.module("wixAngularConstants").constant("letterUnicode", "A-Za-zªµºÀ-ÖØ-öø-ˁˆ-ˑˠ-" + "ˤˬˮͰ-ʹͶͷͺ-ͽΆΈ-ΊΌΎ-ΡΣ-ϵϷ-" + "ҁҊ-ԧԱ-Ֆՙա-ևא-תװ-ײؠ-يٮٯٱ-ۓ" + "ەۥۦۮۯۺ-ۼۿܐܒ-ܯݍ-ޥޱߊ-ߪߴߵߺ" + "ࠀ-ࠕࠚࠤࠨࡀ-ࡘࢠࢢ-ࢬऄ-हऽॐक़-ॡॱ-ॷॹ-" + "ॿঅ-ঌএঐও-নপ-রলশ-হঽৎড়ঢ়য়-ৡৰ" + "ৱਅ-ਊਏਐਓ-ਨਪ-ਰਲਲ਼ਵਸ਼ਸਹਖ਼-ੜਫ਼ੲ-" + "ੴઅ-ઍએ-ઑઓ-નપ-રલળવ-હઽૐૠૡଅ-ଌ" + "ଏଐଓ-ନପ-ରଲଳଵ-ହଽଡ଼ଢ଼ୟ-ୡୱஃஅ-ஊ" + "எ-ஐஒ-கஙசஜஞடணதந-பம-ஹௐఅ-ఌఎ-" + "ఐఒ-నప-ళవ-హఽౘౙౠౡಅ-ಌಎ-ಐಒ-ನಪ-" + "ಳವ-ಹಽೞೠೡೱೲഅ-ഌഎ-ഐഒ-ഺഽൎൠൡൺ-" + "ൿඅ-ඖක-නඳ-රලව-ෆก-ะาำเ-ๆກຂຄ" + "ງຈຊຍດ-ທນ-ຟມ-ຣລວສຫອ-ະາຳຽເ-" + "ໄໆໜ-ໟༀཀ-ཇཉ-ཬྈ-ྌက-ဪဿၐ-ၕၚ-ၝၡၥ" + "ၦၮ-ၰၵ-ႁႎႠ-ჅჇჍა-ჺჼ-ቈቊ-ቍቐ-ቖቘቚ-" + "ቝበ-ኈኊ-ኍነ-ኰኲ-ኵኸ-ኾዀዂ-ዅወ-ዖዘ-ጐጒ-ጕ" + "ጘ-ፚᎀ-ᎏᎠ-Ᏼᐁ-ᙬᙯ-ᙿᚁ-ᚚᚠ-ᛪᜀ-ᜌᜎ-ᜑᜠ-ᜱ" + "ᝀ-ᝑᝠ-ᝬᝮ-ᝰក-ឳៗៜᠠ-ᡷᢀ-ᢨᢪᢰ-ᣵᤀ-ᤜᥐ-" + "ᥭᥰ-ᥴᦀ-ᦫᧁ-ᧇᨀ-ᨖᨠ-ᩔᪧᬅ-ᬳᭅ-ᭋᮃ-ᮠᮮᮯ" + "ᮺ-ᯥᰀ-ᰣᱍ-ᱏᱚ-ᱽᳩ-ᳬᳮ-ᳱᳵᳶᴀ-ᶿḀ-ἕἘ-Ἕ" + "ἠ-ὅὈ-Ὅὐ-ὗὙὛὝὟ-ώᾀ-ᾴᾶ-ᾼιῂ-ῄῆ-ῌ" + "ῐ-ΐῖ-Ίῠ-Ῥῲ-ῴῶ-ῼⁱⁿₐ-ₜℂℇℊ-ℓℕℙ-" + "ℝℤΩℨK-ℭℯ-ℹℼ-ℿⅅ-ⅉⅎↃↄⰀ-Ⱞⰰ-ⱞⱠ-" + "ⳤⳫ-ⳮⳲⳳⴀ-ⴥⴧⴭⴰ-ⵧⵯⶀ-ⶖⶠ-ⶦⶨ-ⶮⶰ-ⶶ" + "ⶸ-ⶾⷀ-ⷆⷈ-ⷎⷐ-ⷖⷘ-ⷞⸯ々〆〱-〵〻〼ぁ-ゖゝ-" + "ゟァ-ヺー-ヿㄅ-ㄭㄱ-ㆎㆠ-ㆺㇰ-ㇿ㐀-䶵一-鿌ꀀ-ꒌꓐ-" + "ꓽꔀ-ꘌꘐ-ꘟꘪꘫꙀ-ꙮꙿ-ꚗꚠ-ꛥꜗ-ꜟꜢ-ꞈꞋ-ꞎꞐ-" + "ꞓꞠ-Ɦꟸ-ꠁꠃ-ꠅꠇ-ꠊꠌ-ꠢꡀ-ꡳꢂ-ꢳꣲ-ꣷꣻꤊ-ꤥ" + "ꤰ-ꥆꥠ-ꥼꦄ-ꦲꧏꨀ-ꨨꩀ-ꩂꩄ-ꩋꩠ-ꩶꩺꪀ-ꪯꪱꪵ" + "ꪶꪹ-ꪽꫀꫂꫛ-ꫝꫠ-ꫪꫲ-ꫴꬁ-ꬆꬉ-ꬎꬑ-ꬖꬠ-ꬦꬨ-" + "ꬮꯀ-ꯢ가-힣ힰ-ퟆퟋ-ퟻ豈-舘並-龎ﬀ-ﬆﬓ-ﬗיִײַ-ﬨ" + "שׁ-זּטּ-לּמּנּסּףּפּצּ-ﮱﯓ-ﴽﵐ-ﶏﶒ-ﷇﷰ-ﷻﹰ-" + "ﹴﹶ-ﻼＡ-Ｚａ-ｚｦ-ﾾￂ-ￇￊ-ￏￒ-ￗￚ-ￜ").constant("numberUnicode", "0-9²³¹¼-¾٠-٩۰-۹߀-߉०-९০-৯৴-৹" + "੦-੯૦-૯୦-୯୲-୷௦-௲౦-౯౸-౾೦-೯൦-൵๐-๙" + "໐-໙༠-༳၀-၉႐-႙፩-፼ᛮ-ᛰ០-៩៰-៹᠐-᠙᥆-᥏" + "᧐-᧚᪀-᪉᪐-᪙᭐-᭙᮰-᮹᱀-᱉᱐-᱙⁰⁴-⁹₀-₉⅐-" + "ↂↅ-↉①-⒛⓪-⓿❶-➓⳽〇〡-〩〸-〺㆒-㆕㈠-㈩㉈-" + "㉏㉑-㉟㊀-㊉㊱-㊿꘠-꘩ꛦ-ꛯ꠰-꠵꣐-꣙꤀-꤉꧐-꧙꩐-" + "꩙꯰-꯹０-９").constant("dashPunctuationUnicode", "-֊־᐀᠆‐-―⸗⸚⸺⸻〜〰゠︱︲﹘﹣－");

"use strict";

var WixRoles = function() {
    function WixRoles() {
        this.OWNER = "owner";
        this.CONTRIBUTOR = "contributor";
        this.LIMITED_ADMIN = "limitedAdmin";
        this.WIX_STAFF = "wixStaff";
        this.BACKOFFICE_MANAGER = "backOfficeManager";
        this.BLOG_EDITOR = "blogEditor";
        this.BLOG_WRITER = "blogWriter";
    }
    return WixRoles;
}();

angular.module("wixAngularPermissionsConstants").constant("wixRoles", new WixRoles());

"use strict";

(function() {
    WixAngularStorageController.$inject = [ "wixCache", "wixStorage" ];
    function WixAngularStorageController(wixCache, wixStorage) {
        var that = this;
        function getOptions() {
            return {
                siteId: that.siteId,
                noCache: that.noCache
            };
        }
        var writeData = function(res) {
            that.data = res && typeof res === "object" ? JSON.stringify(res) : res;
        };
        var eraseData = function() {
            that.data = null;
        };
        this.cache = {
            set: function(key, data) {
                wixCache.set(key, data, getOptions());
            },
            setWithGUID: function(data) {
                wixCache.setWithGUID(data).then(function(key) {
                    that.key = key;
                });
            },
            get: function(key) {
                wixCache.get(key, getOptions()).then(writeData, eraseData);
            },
            getAll: function() {
                wixCache.getAll(getOptions()).then(writeData, eraseData);
            },
            remove: function(key) {
                wixCache.remove(key, getOptions()).then(eraseData);
            }
        };
        this.remote = {
            set: function(key, value) {
                wixStorage.set(key, value, getOptions());
            },
            get: function(key) {
                wixStorage.get(key, getOptions()).then(writeData, eraseData);
            },
            getAll: function() {
                wixStorage.getAll(getOptions()).then(writeData, eraseData);
            },
            remove: function(key) {
                wixStorage.remove(key, getOptions()).then(eraseData);
            }
        };
    }
    angular.module("wixAngularAppInternal").controller("WixAngularStorageController", WixAngularStorageController);
})();

"use strict";

(function() {
    wixTranslateCompile.$inject = [ "$translate", "$compile", "$parse" ];
    function wixTranslateCompile($translate, $compile, $parse) {
        return {
            restrict: "A",
            replace: true,
            link: function(scope, element, attrs) {
                function compileTranslation() {
                    var values = attrs.translateValues ? $parse(attrs.translateValues)(scope) : {};
                    var content = $translate(attrs.wixTranslateCompile, values);
                    element.html(content);
                    $compile(element.contents())(scope);
                }
                compileTranslation();
                scope.$watch(function() {
                    return attrs.wixTranslateCompile;
                }, compileTranslation);
            }
        };
    }
    angular.module("wixAngularTranslateCompile").directive("wixTranslateCompile", wixTranslateCompile);
})();

"use strict";

(function() {
    relativeHref.$inject = [ "wixAngularTopology" ];
    function relativeHref(wixAngularTopology) {
        return {
            priority: 99,
            link: function(scope, element, attr) {
                attr.$observe("relativeHref", function(url) {
                    if (url) {
                        attr.$set("href", wixAngularTopology.staticsUrl + url);
                    }
                });
            }
        };
    }
    angular.module("wixAngularAppInternal").directive("relativeHref", relativeHref);
})();

"use strict";

(function() {
    relativeSrc.$inject = [ "wixAngularTopology" ];
    ngSrc.$inject = [ "wixAngularTopology" ];
    function relativeSrc(wixAngularTopology) {
        return {
            priority: 99,
            link: function(scope, element, attr) {
                attr.$observe("relativeSrc", function(url) {
                    if (url) {
                        attr.$set("src", shouldSetStatics(url) ? wixAngularTopology.staticsUrl + url : url);
                    }
                });
            }
        };
    }
    function ngSrc(wixAngularTopology) {
        return {
            priority: 9999,
            link: function(scope, element, attr) {
                attr.$observe("ngSrc", function(url) {
                    if (shouldSetStatics(url)) {
                        attr.$set("src", wixAngularTopology.staticsUrl + url);
                    }
                });
            }
        };
    }
    function shouldSetStatics(url) {
        return url && (url.indexOf("images/") === 0 || url.indexOf("bower_components/") === 0);
    }
    angular.module("wixAngularAppInternal").directive("relativeSrc", relativeSrc).directive("ngSrc", ngSrc);
})();

"use strict";

(function() {
    wixManagersNgDirective.$inject = [ "manager", "ngDirective", "directiveName", "ngAttributeName", "wixManagerEval" ];
    function hookPreLink(link, fn) {
        if (typeof link === "function") {
            return {
                pre: fn,
                post: link
            };
        } else {
            var hooked = link.pre;
            link.pre = function() {
                fn.apply(undefined, arguments);
                return hooked.apply(undefined, arguments);
            };
            return link;
        }
    }
    function wixManagersNgDirective(manager, ngDirective, directiveName, ngAttributeName, wixManagerEval) {
        var ngDir = ngDirective[0];
        var ddo = angular.copy(ngDir);
        ddo.compile = function() {
            var ret = ngDir.compile.apply(ngDir, arguments);
            return hookPreLink(ret, function(scope, element, attr) {
                attr[ngAttributeName] = function() {
                    var expr = attr[directiveName];
                    return wixManagerEval.eval(manager, expr, attr.wixPermissionContext);
                };
            });
        };
        return ddo;
    }
    function defineNgDirective($injector, manager, name, ngDirective, ngAttributeName) {
        return $injector.invoke(wixManagersNgDirective, this, {
            manager: manager,
            directiveName: name,
            ngDirective: ngDirective,
            ngAttributeName: ngAttributeName
        });
    }
    angular.module("wixAngularExperiments").directive("wixExperimentIf", [ "$injector", "experimentManager", "ngIfDirective", function($injector, experimentManager, ngIfDirective) {
        return defineNgDirective($injector, experimentManager, "wixExperimentIf", ngIfDirective, "ngIf");
    } ]);
    angular.module("wixAngularPermissions").directive("wixPermissionIf", [ "$injector", "permissionsManager", "ngIfDirective", function($injector, permissionsManager, ngIfDirective) {
        return defineNgDirective($injector, permissionsManager, "wixPermissionIf", ngIfDirective, "ngIf");
    } ]);
    angular.module("wixAngularExperiments").directive("wixExperimentDisabled", [ "$injector", "experimentManager", "ngDisabledDirective", function($injector, experimentManager, ngDisabledDirective) {
        return defineNgDirective($injector, experimentManager, "wixExperimentDisabled", ngDisabledDirective, "ngDisabled");
    } ]);
    angular.module("wixAngularPermissions").directive("wixPermissionDisabled", [ "$injector", "permissionsManager", "ngDisabledDirective", function($injector, permissionsManager, ngDisabledDirective) {
        return defineNgDirective($injector, permissionsManager, "wixPermissionDisabled", ngDisabledDirective, "ngDisabled");
    } ]);
})();

"use strict";

(function() {
    wixManagersClass.$inject = [ "manager", "directiveName", "valuesToCheck", "$parse", "wixConstValue" ];
    function wixManagersClass(manager, directiveName, valuesToCheck, $parse, wixConstValue) {
        return {
            restrict: "A",
            link: function postLink(scope, element, attr) {
                var values = $parse(attr[valuesToCheck])(scope);
                var name = attr[directiveName];
                if (values) {
                    var classToAdd = values[manager.get(wixConstValue(name))];
                    if (classToAdd) {
                        element.addClass(classToAdd);
                    }
                }
            }
        };
    }
    function defineClassDirective($injector, manager, name, valuesToCheck) {
        return $injector.invoke(wixManagersClass, this, {
            manager: manager,
            directiveName: name,
            valuesToCheck: valuesToCheck
        });
    }
    angular.module("wixAngularExperiments").directive("wixExperimentClass", [ "$injector", "experimentManager", function($injector, experimentManager) {
        return defineClassDirective($injector, experimentManager, "wixExperimentClass", "experimentValues");
    } ]);
    angular.module("wixAngularPermissions").directive("wixPermissionClass", [ "$injector", "permissionsManager", function($injector, permissionsManager) {
        return defineClassDirective($injector, permissionsManager, "wixPermissionClass", "permissionValues");
    } ]);
})();

"use strict";

(function() {
    wixMailValidator.$inject = [ "letterUnicode", "numberUnicode", "dashPunctuationUnicode" ];
    function wixMailValidator(letterUnicode, numberUnicode, dashPunctuationUnicode) {
        var L = letterUnicode;
        var N = numberUnicode;
        var Pd = dashPunctuationUnicode;
        var ALL_VALID_CHARS_EXCEPT_DOT = "([" + N + L + Pd + "_+/!#$%&'*=?^`{}|~])";
        var EMAIL_LOCAL_PART_REGEX = ALL_VALID_CHARS_EXCEPT_DOT + "([" + N + L + Pd + "._+/!#$%&'*=?^`{}|~])*" + ALL_VALID_CHARS_EXCEPT_DOT;
        var EMAIL_DOMAIN_REGEX = "[0-9a-zA-Z]+([-.]+[0-9a-zA-Z]+)*([0-9a-zA-Z]*[.])[a-zA-Z]{2,63}";
        var EMAIL_REGEXP = new RegExp("^(" + ALL_VALID_CHARS_EXCEPT_DOT + "|" + EMAIL_LOCAL_PART_REGEX + ")@" + EMAIL_DOMAIN_REGEX + "$");
        return {
            require: "ngModel",
            restrict: "A",
            link: function(scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function(viewValue) {
                    if (!viewValue || EMAIL_REGEXP.test(viewValue)) {
                        ctrl.$setValidity("wixMailValidator", true);
                        return viewValue;
                    } else {
                        ctrl.$setValidity("wixMailValidator", false);
                        return undefined;
                    }
                });
            }
        };
    }
    angular.module("wixAngularAppInternal").directive("wixMailValidator", wixMailValidator);
})();

"use strict";

(function() {
    angular.module("wixAngularAppInternal").factory("wixConstValue", [ "$injector", function($injector) {
        return function(name) {
            var constantValueOrName = $injector.has(name) ? $injector.get(name) : name;
            return constantValueOrName;
        };
    } ]);
})();

"use strict";

(function() {
    wixAngularInterceptorFactory.$inject = [ "$q", "wixCookies", "$rootScope", "wixAngularEvents", "wixAngularTopology", "$injector" ];
    decorateHttpBackend.$inject = [ "$provide" ];
    function wixAngularInterceptorFactory($q, wixCookies, $rootScope, wixAngularEvents, wixAngularTopology, $injector) {
        var wixAngularInterceptor = {};
        var firstUserSwitchTest = true;
        var previousUserGUID;
        var cbMap = {};
        function errorHandler(response) {
            return $q.reject(response);
        }
        function checkUserSwitch() {
            if (!firstUserSwitchTest && previousUserGUID !== wixCookies.userGUID) {
                $rootScope.$emit(wixAngularEvents.userSwitch, wixCookies.userGUID, previousUserGUID);
            }
            previousUserGUID = wixCookies.userGUID;
            firstUserSwitchTest = false;
        }
        function generateCacheBustingValue(cache, url) {
            if (cache) {
                cbMap[url] = cbMap[url] || generateCacheBustingValue();
                return cbMap[url];
            } else {
                return Math.floor(Math.random() * 9e4) + 1e4;
            }
        }
        wixAngularInterceptor.request = function(config) {
            checkUserSwitch();
            if (config.url.match(/\.(?:html|svg)$/)) {
                if (!config.url.match(/(:|^)\/\//)) {
                    if (!config.cache || !config.cache.get || !config.cache.get(config.url)) {
                        config.url = wixAngularTopology.calcPartialsUrl(wixAngularTopology.staticsUrl) + config.url.replace(/^\//, "");
                    }
                }
            } else if (config.url.indexOf("/_api/") === 0) {
                config.url = wixAngularTopology.fixOrigin(config.url);
                if (config.method === "GET" && $injector.has("experimentManager") && $injector.get("experimentManager").isExperimentEnabled("specs.wos.CachBustingAPI")) {
                    config.url = URI(config.url).addSearch("cb339", generateCacheBustingValue(config.cache, config.url)).toString();
                }
                if (wixAngularTopology.instance && !config.headers["X-Wix-Instance"]) {
                    config.headers["X-Wix-Instance"] = wixAngularTopology.instance;
                }
            }
            return config;
        };
        wixAngularInterceptor.response = function(response) {
            if (response.data) {
                if (response.data.success === false) {
                    response.status = 500;
                    return errorHandler(response);
                } else if (response.data.success === true && response.data.payload !== undefined) {
                    if (angular.isObject(response.data.payload) && !angular.isArray(response.data.payload)) {
                        var payload = response.data.payload;
                        delete response.data.payload;
                        delete response.data.success;
                        delete response.data.errorCode;
                        delete response.data.errorDescription;
                        response.data = angular.extend(response.data, payload);
                    } else {
                        response.data = response.data.payload;
                    }
                }
            }
            return response;
        };
        wixAngularInterceptor.responseError = function(response) {
            return errorHandler(response);
        };
        return wixAngularInterceptor;
    }
    angular.module("wixAngularAppInternal").factory("wixAngularInterceptor", wixAngularInterceptorFactory).constant("wixAngularEvents", {
        userSwitch: "userSwitch"
    });
    function decorateHttpBackend($provide) {
        $provide.decorator("$httpBackend", [ "$delegate", function($delegate) {
            [ "expect", "when" ].forEach(function(prefix) {
                var hooked = $delegate[prefix];
                $delegate[prefix] = function(method, url) {
                    if (typeof url === "string") {
                        var str = url;
                        arguments[1] = {
                            test: function(input) {
                                return URI(input).removeSearch("cb339").equals(str);
                            },
                            toString: function() {
                                return str;
                            }
                        };
                    }
                    return hooked.apply(this, arguments);
                };
            });
            return $delegate;
        } ]);
    }
    try {
        angular.module("ngMock").config(decorateHttpBackend);
    } catch (e) {}
    try {
        angular.module("ngMockE2E").config(decorateHttpBackend);
    } catch (e) {}
})();

"use strict";

(function() {
    WixAngular.$inject = [ "wixAngularTopologyProvider", "experimentManagerProvider", "nrNgClientProvider" ];
    function WixAngular(wixAngularTopologyProvider, experimentManagerProvider, nrNgClientProvider) {
        this.getStaticsUrl = wixAngularTopologyProvider.getStaticsUrl;
        this.setStaticsUrl = wixAngularTopologyProvider.setStaticsUrl;
        var isExperimentEnabled = experimentManagerProvider.isExperimentEnabled.bind(experimentManagerProvider);
        this.setExperiments = experimentManagerProvider.setExperiments.bind(experimentManagerProvider);
        this.isExperimentEnabled = isExperimentEnabled;
        this.debugEnabled = function(debug) {
            nrNgClientProvider.reportFinishedEnabled(!debug);
            nrNgClientProvider.reportErrorsEnabled(!debug);
        };
        this.$get = [ "wixAngularTopology", "experimentManager", function(wixAngularTopology, experimentManager) {
            var wixAngular = {};
            wixAngular.experiments = experimentManager.$$getExperimentsObj();
            wixAngular.isExperimentEnabled = isExperimentEnabled;
            wixAngular.fixOrigin = wixAngularTopology.fixOrigin;
            wixAngular.staticsUrl = wixAngularTopology.staticsUrl;
            wixAngular.partialsUrl = wixAngularTopology.partialsUrl;
            return wixAngular;
        } ];
        this.$get.$inject = [ "wixAngularTopology", "experimentManager" ];
    }
    angular.module("wixAngularBackwardCompatibility").provider("wixAngular", WixAngular);
})();

"use strict";

var WixCache = function() {
    WixCache.$inject = [ "provider", "$q", "recordUtils", "cleanableStorage", "wixAngularStorageErrors", "DEFAULT_AGE_IN_SEC", "DATA_TYPE", "ADHOC_TYPE", "REMOTE_TYPE", "CLEAN_EPSILON" ];
    function WixCache(provider, $q, recordUtils, cleanableStorage, wixAngularStorageErrors, DEFAULT_AGE_IN_SEC, DATA_TYPE, ADHOC_TYPE, REMOTE_TYPE, CLEAN_EPSILON) {
        this.$q = $q;
        this.recordUtils = recordUtils;
        this.cleanableStorage = cleanableStorage;
        this.wixAngularStorageErrors = wixAngularStorageErrors;
        this.DEFAULT_AGE_IN_SEC = DEFAULT_AGE_IN_SEC;
        this.DATA_TYPE = DATA_TYPE;
        this.ADHOC_TYPE = ADHOC_TYPE;
        this.REMOTE_TYPE = REMOTE_TYPE;
        this.CLEAN_EPSILON = CLEAN_EPSILON;
        this.namespace = provider.namespace;
    }
    WixCache.prototype.rejectUserNotLoggedIn = function() {
        return this.$q.reject(this.wixAngularStorageErrors.LOGGED_OUT);
    };
    WixCache.prototype.rejectWithRuntimeException = function() {
        return this.$q.reject(this.wixAngularStorageErrors.RUNTIME_EXCEPTION);
    };
    WixCache.prototype.tryToSet = function(key, value) {
        var _this = this;
        var cacheKey = this.recordUtils.getCacheKey(key, value.options);
        return this.cleanableStorage.set(cacheKey, value).then(function() {
            return key;
        }, function(reason) {
            if (reason === _this.wixAngularStorageErrors.RUNTIME_EXCEPTION) {
                return _this.rejectWithRuntimeException();
            }
            if (value.options.type === _this.REMOTE_TYPE) {
                return _this.$q.reject();
            } else {
                return _this.cleanableStorage.clear(_this.recordUtils.getRecordSize(cacheKey, value) + _this.CLEAN_EPSILON).then(function() {
                    return _this.cleanableStorage.set(cacheKey, value).then(function() {
                        return key;
                    }, function() {
                        return _this.rejectWithRuntimeException();
                    });
                }, function() {
                    return _this.$q.reject(_this.wixAngularStorageErrors.QUOTA_EXCEEDED);
                });
            }
        });
    };
    WixCache.prototype.withNamespace = function(opts) {
        var options = angular.extend({}, {
            namespace: this.namespace
        }, opts);
        this.recordUtils.validateNamespace(options);
        return options;
    };
    WixCache.prototype.set = function(key, data, options) {
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        options = this.withNamespace(options);
        this.recordUtils.validateKey(key);
        this.recordUtils.validateData(data);
        this.recordUtils.validateExpiration(options);
        var value = {
            createdAt: Date.now(),
            data: data,
            options: angular.extend({
                expiration: this.DEFAULT_AGE_IN_SEC,
                type: this.DATA_TYPE
            }, options)
        };
        return this.tryToSet(key, value);
    };
    WixCache.prototype.setWithGUID = function(data, opts) {
        if (opts === void 0) {
            opts = {};
        }
        var key = this.recordUtils.generateRandomKey();
        return this.set(key, data, angular.extend({
            expiration: null,
            type: this.ADHOC_TYPE
        }, opts));
    };
    WixCache.prototype.get = function(key, opts) {
        var _this = this;
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        opts = this.withNamespace(opts);
        return this.cleanableStorage.get(this.recordUtils.getCacheKey(key, opts)).then(function(record) {
            if (record && !_this.recordUtils.isExpired(record)) {
                return record.data;
            } else {
                return _this.$q.reject(_this.wixAngularStorageErrors.NOT_FOUND);
            }
        }, function() {
            return _this.rejectWithRuntimeException();
        });
    };
    WixCache.prototype.getAll = function(opts) {
        var _this = this;
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        opts = this.withNamespace(opts);
        return this.cleanableStorage.getAllWithPrefix(this.recordUtils.getCachePrefix(opts)).then(function(records) {
            var cacheRecords = {};
            Object.keys(records).forEach(function(key) {
                if (records[key] && !_this.recordUtils.isExpired(records[key])) {
                    var originKey = _this.recordUtils.getOriginKey(key);
                    cacheRecords[originKey] = records[key].data;
                }
            });
            return cacheRecords;
        }, function() {
            return _this.rejectWithRuntimeException();
        });
    };
    WixCache.prototype.remove = function(key, opts) {
        var _this = this;
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        opts = this.withNamespace(opts);
        return this.cleanableStorage.del(this.recordUtils.getCacheKey(key, opts)).catch(function() {
            return _this.rejectWithRuntimeException();
        });
    };
    return WixCache;
}();

var WixCacheProvider = function() {
    function WixCacheProvider() {}
    WixCacheProvider.prototype.setNamespace = function(namespace) {
        this.namespace = namespace;
    };
    WixCacheProvider.prototype.$get = function($injector) {
        return $injector.instantiate(WixCache, {
            provider: this
        });
    };
    WixCacheProvider.prototype.$get.$inject = [ "$injector" ];
    return WixCacheProvider;
}();

angular.module("wixAngularAppInternal").provider("wixCache", WixCacheProvider);

"use strict";

(function() {
    wixCookiesFactory.$inject = [ "cookieStr" ];
    function wixCookiesFactory(cookieStr) {
        var parsedUser, prevCookies;
        function parseUserCookie(cookie) {
            var cookieParts = cookie ? cookie.split("|") : [];
            return {
                guid: cookieParts[6],
                userName: cookieParts[0]
            };
        }
        function parseAllCookies(cookies) {
            return cookies.split(";").map(function(str) {
                return str.trim();
            }).reduce(function(acc, curr) {
                var args = curr.split("=");
                acc[args[0]] = args[1];
                return acc;
            }, {});
        }
        function getParsedUserCookie() {
            var cookies = cookieStr() || "";
            if (cookies !== prevCookies) {
                prevCookies = cookies;
                parsedUser = parseUserCookie(parseAllCookies(cookies).wixClient);
            }
            return parsedUser;
        }
        return {
            get userGUID() {
                return getParsedUserCookie().guid;
            },
            get userName() {
                return getParsedUserCookie().userName;
            }
        };
    }
    angular.module("wixAngularAppInternal").factory("wixCookies", wixCookiesFactory).factory("cookieStr", [ "$document", function($document) {
        return function() {
            return $document[0] && $document[0].cookie || "";
        };
    } ]);
})();

"use strict";

(function() {
    cleanableStorage.$inject = [ "$interval", "$q", "recordUtils", "DATA_TYPE", "CLEANING_INTERVAL", "MAX_STORAGE_SIZE_IN_BYTES" ];
    function cleanableStorage($interval, $q, recordUtils, DATA_TYPE, CLEANING_INTERVAL, MAX_STORAGE_SIZE_IN_BYTES) {
        var dataKeys = [];
        var remoteAndAdhocKeys = [];
        function getValue(key) {
            return localStorage[key] && JSON.parse(localStorage[key]);
        }
        function clearRecord(key) {
            var record = getValue(key);
            if (record) {
                var recordSize = recordUtils.getRecordSize(key, record);
                delete localStorage[key];
                return recordSize;
            } else {
                return 0;
            }
        }
        function clearRecords(keys) {
            return keys.reduce(function(acc, key) {
                acc += clearRecord(key);
                return acc;
            }, 0);
        }
        function getWixCacheKeys() {
            return Object.keys(localStorage).filter(recordUtils.hasPrefix);
        }
        function getAllKeysAndValues(prefix) {
            var cacheStorage = {};
            var keys = Object.keys(localStorage).filter(function(key) {
                return key.indexOf(prefix) === 0;
            });
            keys.forEach(function(key) {
                cacheStorage[key] = getValue(key);
            });
            return cacheStorage;
        }
        function getWixCacheSize() {
            return getWixCacheKeys().reduce(function(acc, key) {
                return acc + recordUtils.getRecordSize(key, getValue(key));
            }, 0);
        }
        function loadExistingWixCacheKeys() {
            var createdAtSort = function(a, b) {
                return a.createdAt - b.createdAt;
            };
            var getKey = function(item) {
                return item.key;
            };
            dataKeys = [];
            remoteAndAdhocKeys = [];
            getWixCacheKeys().forEach(function(key) {
                var item = getValue(key);
                var arr = item.options.type === DATA_TYPE ? dataKeys : remoteAndAdhocKeys;
                arr.push({
                    key: key,
                    createdAt: item.createdAt
                });
            });
            dataKeys.sort(createdAtSort);
            remoteAndAdhocKeys.sort(createdAtSort);
            dataKeys = dataKeys.map(getKey);
            remoteAndAdhocKeys = remoteAndAdhocKeys.map(getKey);
        }
        function clearOtherUsers() {
            return clearRecords(getWixCacheKeys().filter(function(key) {
                return !recordUtils.belongsToCurrentUser(key);
            }));
        }
        function clearExpiredRecords() {
            return clearRecords(getWixCacheKeys().filter(function(cacheKey) {
                var record = getValue(cacheKey);
                return recordUtils.isExpired(record);
            }));
        }
        function clearNonExpiredRecord() {
            var arr = remoteAndAdhocKeys.length === 0 ? dataKeys : remoteAndAdhocKeys;
            var key = arr.shift();
            return clearRecord(key);
        }
        function clear(amount) {
            var requiredSpace = amount || 0;
            var clearedSpace = 0;
            clearedSpace += clearOtherUsers();
            clearedSpace += clearExpiredRecords();
            var size = getWixCacheSize();
            var removedRecordsSpace = 0;
            loadExistingWixCacheKeys();
            while (size - removedRecordsSpace > MAX_STORAGE_SIZE_IN_BYTES) {
                var removed = clearNonExpiredRecord();
                clearedSpace += removed;
                removedRecordsSpace += removed;
            }
            if (size - removedRecordsSpace < requiredSpace - clearedSpace) {
                return false;
            }
            while (clearedSpace < requiredSpace) {
                clearedSpace += clearNonExpiredRecord();
            }
            return true;
        }
        function promiseWrapper(fn) {
            var defer = $q.defer();
            try {
                var done;
                var result = fn(function() {
                    done = true;
                    defer.resolve();
                }, function() {
                    done = true;
                    defer.reject();
                });
                if (!done) {
                    defer.resolve(result);
                }
            } catch (e) {
                defer.reject();
            }
            return defer.promise;
        }
        clear();
        $interval(function() {
            clear();
        }, CLEANING_INTERVAL);
        return {
            set: function(key, value) {
                return promiseWrapper(function() {
                    localStorage[key] = JSON.stringify(value);
                });
            },
            get: function(key) {
                return promiseWrapper(function() {
                    return getValue(key);
                });
            },
            getAllWithPrefix: function(prefix) {
                return promiseWrapper(function() {
                    return getAllKeysAndValues(prefix);
                });
            },
            del: function(key) {
                return promiseWrapper(function() {
                    delete localStorage[key];
                });
            },
            clear: function(amount) {
                return promiseWrapper(function(resolve, reject) {
                    if (clear(amount)) {
                        resolve();
                    } else {
                        reject();
                    }
                });
            }
        };
    }
    angular.module("wixAngularStorage").factory("cleanableStorage", cleanableStorage);
})();

"use strict";

var WixStorage = function() {
    WixStorage.$inject = [ "provider", "$q", "$http", "recordUtils", "wixCache", "wixAngularStorageErrors", "ANGULAR_STORAGE_PREFIX", "REMOTE_TYPE", "DEFAULT_AGE_IN_SEC" ];
    function WixStorage(provider, $q, $http, recordUtils, wixCache, wixAngularStorageErrors, ANGULAR_STORAGE_PREFIX, REMOTE_TYPE, DEFAULT_AGE_IN_SEC) {
        this.$q = $q;
        this.$http = $http;
        this.recordUtils = recordUtils;
        this.wixCache = wixCache;
        this.wixAngularStorageErrors = wixAngularStorageErrors;
        this.ANGULAR_STORAGE_PREFIX = ANGULAR_STORAGE_PREFIX;
        this.REMOTE_TYPE = REMOTE_TYPE;
        this.DEFAULT_AGE_IN_SEC = DEFAULT_AGE_IN_SEC;
        this.namespace = provider.namespace;
    }
    WixStorage.prototype.rejectUserNotLoggedIn = function() {
        return this.$q.reject(this.wixAngularStorageErrors.LOGGED_OUT);
    };
    WixStorage.prototype.cacheRemoteData = function(key, data, options) {
        if (!options.noCache) {
            return this.wixCache.set(key, data, angular.extend({}, options, {
                type: this.REMOTE_TYPE,
                expiration: this.DEFAULT_AGE_IN_SEC
            }));
        }
    };
    WixStorage.prototype.getUrl = function(path, options, key) {
        return [ "/_api/wix-user-preferences-webapp", path, options.namespace, options.siteId, key ].filter(angular.identity).join("/");
    };
    WixStorage.prototype.getRemote = function(key, options) {
        var _this = this;
        var path = options.siteId ? "getVolatilePrefForSite" : "getVolatilePrefForKey";
        var namespace = options.namespace;
        var url = this.getUrl(path, options, key);
        return this.$http.get(url).then(function(res) {
            if (res.data[key] === null) {
                return _this.rejectNotFound();
            }
            _this.cacheRemoteData(key, res.data[key], options);
            return res.data[key];
        }, function(err) {
            if (err.status === 404) {
                if (namespace !== _this.ANGULAR_STORAGE_PREFIX) {
                    return _this.handleNamespaceMigration(key, options);
                } else {
                    return _this.rejectNotFound();
                }
            }
            return _this.$q.reject(_this.wixAngularStorageErrors.SERVER_ERROR);
        });
    };
    WixStorage.prototype.getAllRemote = function(options) {
        var _this = this;
        var path = options.siteId ? "getVolatilePrefsForSite" : "getVolatilePrefs";
        var url = this.getUrl(path, options, undefined);
        return this.$http.get(url).then(function(res) {
            Object.keys(res.data).forEach(function(key) {
                return _this.cacheRemoteData(key, res.data[key], options);
            });
            return res.data;
        });
    };
    WixStorage.prototype.handleNamespaceMigration = function(key, options) {
        var _this = this;
        var newOptions = angular.extend({}, options, {
            namespace: this.ANGULAR_STORAGE_PREFIX,
            noCache: true
        });
        return this.getRemote(key, newOptions).then(function(data) {
            _this.cacheRemoteData(key, data, options);
            return _this.set(key, data, options).then(function() {
                return data;
            });
        }, function(error) {
            if (error === _this.wixAngularStorageErrors.NOT_FOUND) {
                _this.cacheRemoteData(key, null, options);
            }
            return _this.$q.reject(error);
        });
    };
    WixStorage.prototype.tryCache = function(key, options) {
        var _this = this;
        return this.wixCache.get(key, options).then(function(res) {
            return res || _this.rejectNotFound();
        }, function() {
            return _this.getRemote(key, options);
        });
    };
    WixStorage.prototype.tryCacheGetAll = function(options) {
        var _this = this;
        return this.wixCache.getAll(options).then(function(res) {
            return res || _this.rejectNotFound();
        }, function() {
            return _this.getAllRemote(options);
        });
    };
    WixStorage.prototype.rejectNotFound = function() {
        return this.$q.reject(this.wixAngularStorageErrors.NOT_FOUND);
    };
    WixStorage.prototype.withNamespace = function(opts) {
        var options = angular.extend({}, {
            namespace: this.namespace
        }, opts);
        this.recordUtils.validateNamespace(options);
        return options;
    };
    WixStorage.prototype.set = function(key, data, opts) {
        var _this = this;
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        var options = this.withNamespace(opts);
        this.recordUtils.validateKey(key);
        this.recordUtils.validateData(data);
        this.recordUtils.validateExpiration(options);
        var dto = {
            nameSpace: options.namespace,
            key: key,
            blob: data
        };
        if (options.siteId) {
            dto.siteId = options.siteId;
        }
        if (options.expiration) {
            dto.TTLInDays = Math.ceil(options.expiration / (60 * 60 * 24));
        }
        return this.$http.post("/_api/wix-user-preferences-webapp/set", dto).then(function() {
            _this.cacheRemoteData(key, data, options);
            return key;
        });
    };
    WixStorage.prototype.get = function(key, opts) {
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        var options = this.withNamespace(opts);
        return !options.noCache ? this.tryCache(key, options) : this.getRemote(key, options);
    };
    WixStorage.prototype.getAll = function(opts) {
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        var options = this.withNamespace(opts);
        return !options.noCache ? this.tryCacheGetAll(options) : this.getAllRemote(options);
    };
    WixStorage.prototype.remove = function(key, opts) {
        if (!this.recordUtils.isUserLoggedIn()) {
            return this.rejectUserNotLoggedIn();
        }
        return this.set(key, null, opts);
    };
    return WixStorage;
}();

var WixStorageProvider = function() {
    function WixStorageProvider() {}
    WixStorageProvider.prototype.setNamespace = function(namespace) {
        this.namespace = namespace;
    };
    WixStorageProvider.prototype.$get = function($injector) {
        return $injector.instantiate(WixStorage, {
            provider: this
        });
    };
    WixStorageProvider.prototype.$get.$inject = [ "$injector" ];
    return WixStorageProvider;
}();

angular.module("wixAngularStorage").provider("wixStorage", WixStorageProvider);

"use strict";

(function() {
    recordUtilsFactory.$inject = [ "wixCookies", "ANGULAR_STORAGE_PREFIX", "KEY_SEPARATOR", "MAX_KEY_LENGTH", "MAX_VALUE_SIZE_IN_BYTES", "MAX_AGE_IN_SEC" ];
    function recordUtilsFactory(wixCookies, ANGULAR_STORAGE_PREFIX, KEY_SEPARATOR, MAX_KEY_LENGTH, MAX_VALUE_SIZE_IN_BYTES, MAX_AGE_IN_SEC) {
        var recordUtils = {};
        function countBytes(str) {
            return encodeURI(str).match(/%..|./g).length;
        }
        function hasExpiration(options) {
            return options && !!options.expiration;
        }
        recordUtils.isUserLoggedIn = function() {
            return wixCookies.userGUID !== undefined;
        };
        recordUtils.validateKey = function(key) {
            if (typeof key !== "string" || key.length > MAX_KEY_LENGTH) {
                throw new Error("Key length should be no more than " + MAX_KEY_LENGTH + " chars");
            }
        };
        recordUtils.validateData = function(data) {
            var val = JSON.stringify(data);
            if (countBytes(val) > MAX_VALUE_SIZE_IN_BYTES) {
                throw new Error("The size of passed data exceeds the allowed " + MAX_VALUE_SIZE_IN_BYTES / 1024 + " KB");
            }
        };
        recordUtils.validateExpiration = function(options) {
            if (hasExpiration(options) && (typeof options.expiration !== "number" || options.expiration > MAX_AGE_IN_SEC)) {
                throw new Error("Expiration should be a number and cannot increase " + MAX_AGE_IN_SEC + " seconds");
            }
        };
        recordUtils.validateNamespace = function(options) {
            if (!options.namespace) {
                throw new Error("namespace is required");
            } else if (typeof options.namespace !== "string") {
                throw new Error("namespace should be a string");
            }
        };
        recordUtils.isExpired = function(record) {
            if (hasExpiration(record.options)) {
                return record.createdAt + record.options.expiration * 1e3 <= Date.now();
            } else {
                return false;
            }
        };
        recordUtils.getRecordSize = function(key, value) {
            return countBytes(key) + countBytes(JSON.stringify(value));
        };
        recordUtils.getCachePrefix = function(opts) {
            var options = opts || {};
            return [ ANGULAR_STORAGE_PREFIX, wixCookies.userGUID, options.siteId, options.namespace ].filter(angular.identity).join(KEY_SEPARATOR) + KEY_SEPARATOR;
        };
        recordUtils.getCacheKey = function(key, opts) {
            return recordUtils.getCachePrefix(opts) + key;
        };
        recordUtils.getOriginKey = function(key) {
            return key.split(KEY_SEPARATOR).pop();
        };
        recordUtils.generateRandomKey = function() {
            return Math.random().toString(36).slice(2);
        };
        recordUtils.hasPrefix = function(key) {
            return key.indexOf(ANGULAR_STORAGE_PREFIX) === 0;
        };
        recordUtils.belongsToCurrentUser = function(key) {
            if (recordUtils.isUserLoggedIn()) {
                return key.split(KEY_SEPARATOR)[1] === wixCookies.userGUID;
            } else {
                return false;
            }
        };
        return recordUtils;
    }
    angular.module("wixAngularStorage").factory("recordUtils", recordUtilsFactory);
})();

"use strict";

(function() {
    WixAngularTopology.$inject = [ "$sceDelegateProvider", "nrNgClientProvider" ];
    function WixAngularTopology($sceDelegateProvider, nrNgClientProvider) {
        var staticsUrl = "";
        var instance;
        this.getStaticsUrl = function() {
            return staticsUrl;
        };
        this.setStaticsUrl = function(url) {
            staticsUrl = url && url.replace(/\/?$/, "/").replace(/^\/\//, location.protocol + "//");
            $sceDelegateProvider.resourceUrlWhitelist([ staticsUrl + "**", "self" ]);
            nrNgClientProvider.tag("staticUrl", staticsUrl);
        };
        this.setInstance = function(_instance) {
            instance = _instance;
        };
        this.$get = [ "$window", "$document", "$location", function($window, $document, $location) {
            var origin = $document.find && $document.find("base").attr("href") ? $window.location.protocol + "//" + $window.location.host : "";
            function isStaging() {
                return /\.wixpress\.com$/.test($window.location.hostname);
            }
            function fixOrigin(url) {
                return url.replace(/^([^\/]*\/\/+)?[^\/]*/, origin);
            }
            var wixAngularTopology = {};
            wixAngularTopology.fixOrigin = fixOrigin;
            wixAngularTopology.calcPartialsUrl = function(staticsUrl, force) {
                if (!force && $location.protocol && $location.protocol() === "https") {
                    return staticsUrl;
                } else {
                    return staticsUrl ? fixOrigin(staticsUrl.replace("/services/", "/_partials/")) : "";
                }
            };
            wixAngularTopology.staticsUrl = staticsUrl ? staticsUrl : "";
            wixAngularTopology.partialsUrl = staticsUrl ? fixOrigin(staticsUrl.replace("/services/", "/_partials/")) : "";
            wixAngularTopology.isStaging = isStaging;
            wixAngularTopology.instance = instance;
            return wixAngularTopology;
        } ];
        this.$get.$inject = [ "$window", "$document", "$location" ];
    }
    angular.module("wixAngularAppInternal").provider("wixAngularTopology", WixAngularTopology);
})();

"use strict";

window.jsonpExperiemts = {};

window.loadExperimentScopeSync = function(scope) {
    var url = "//www.wix.com/_api/wix-laboratory-server/laboratory/conductAllInScope?scope=" + scope + "&accept=jsonp&callback=setExperimentsSync";
    document.write('<script src="' + url + '"></script>');
    window.setExperimentsSync = function(junk, experiments) {
        angular.extend(window.jsonpExperiemts, experiments);
    };
};

var ExperimentManager = function() {
    ExperimentManager.$inject = [ "provider", "$http" ];
    function ExperimentManager(provider, $http) {
        this.provider = provider;
        this.$http = $http;
        this.petriUrlPrefix = "/_api/wix-laboratory-server/laboratory/";
        this.getExperimentValue = provider.getExperimentValue.bind(provider);
        this.isExperimentEnabled = provider.isExperimentEnabled.bind(provider);
    }
    ExperimentManager.prototype.get = function(value) {
        return this.getExperimentValue(value);
    };
    ExperimentManager.prototype.contains = function(value) {
        return this.isExperimentEnabled(value);
    };
    ExperimentManager.prototype.loadScope = function(scope) {
        var _this = this;
        return this.$$queryPetri({
            scope: scope
        }).then(function(experiments) {
            _this.provider.setExperiments(experiments);
            return experiments;
        });
    };
    ExperimentManager.prototype.loadExperiment = function(name, fallback) {
        var _this = this;
        return this.$$queryPetri({
            name: name,
            fallback: fallback
        }).then(function(value) {
            var singleExperiment = {};
            singleExperiment[name] = value;
            _this.provider.setExperiments(singleExperiment);
            return value;
        });
    };
    ExperimentManager.prototype.$$queryPetri = function(params) {
        return this.$http.get(this.getPetriUrl(params), {
            params: this.getQueryParams(params),
            cache: true
        }).then(function(result) {
            return result.data;
        });
    };
    ExperimentManager.prototype.$$getExperimentsObj = function() {
        return this.provider.experiments;
    };
    ExperimentManager.prototype.getPetriUrl = function(params) {
        return this.petriUrlPrefix + (params.scope ? "conductAllInScope/" : "conductExperiment/");
    };
    ExperimentManager.prototype.getQueryParams = function(params) {
        return params.scope ? {
            scope: params.scope
        } : {
            key: params.name,
            fallback: params.fallback
        };
    };
    return ExperimentManager;
}();

var ExperimentManagerProvider = function() {
    function ExperimentManagerProvider() {
        this.experiments = angular.copy(window.jsonpExperiemts);
    }
    ExperimentManagerProvider.prototype.clearExperiments = function() {
        this.experiments = {};
    };
    ExperimentManagerProvider.prototype.isExperimentEnabled = function(name) {
        return this.experiments[name] === "true";
    };
    ExperimentManagerProvider.prototype.setExperiments = function(map) {
        angular.extend(this.experiments, map);
    };
    ExperimentManagerProvider.prototype.getExperimentValue = function(name) {
        return this.experiments[name];
    };
    ExperimentManagerProvider.prototype.$get = function($injector) {
        return $injector.instantiate(ExperimentManager, {
            provider: this
        });
    };
    ExperimentManagerProvider.prototype.$get.$inject = [ "$injector" ];
    return ExperimentManagerProvider;
}();

angular.module("wixAngularExperiments").provider("experimentManager", ExperimentManagerProvider).run([ "$rootScope", "experimentManager", function($rootScope, experimentManager) {
    $rootScope.experimentManager = experimentManager;
} ]);

"use strict";

if (window.beforeEach) {
    window.beforeEach(function() {
        angular.module("experimentManagerMock").config([ "experimentManagerProvider", function(experimentManagerProvider) {
            experimentManagerProvider.clearExperiments();
        } ]);
    });
}

angular.module("experimentManagerMock", []).config([ "$provide", function($provide) {
    $provide.decorator("experimentManager", [ "$delegate", "$q", function($delegate, $q) {
        var originalGetExperimentValue = $delegate.getExperimentValue.bind($delegate);
        var originalIsExperimentEnabled = $delegate.isExperimentEnabled.bind($delegate);
        var scopeToExperiments = {};
        var unexpected = [];
        var used = [];
        function addIfNotExist(val, group) {
            if (group.indexOf(val) === -1) {
                group.push(val);
            }
        }
        function markAsUsedOrUnexpected(experiment) {
            if (originalGetExperimentValue(experiment) === undefined) {
                addIfNotExist(experiment, unexpected);
            } else {
                addIfNotExist(experiment, used);
            }
        }
        function resolvePromise(params) {
            var deferred = $q.defer();
            if (params.scope) {
                deferred.resolve(scopeToExperiments[params.scope] || {});
            } else {
                deferred.resolve(Object.keys(scopeToExperiments).reduce(function(prev, scope) {
                    return prev || scopeToExperiments[scope][params.name];
                }, undefined) || params.fallback);
            }
            return deferred.promise;
        }
        $delegate.getExperimentValue = function(name) {
            markAsUsedOrUnexpected(name);
            return originalGetExperimentValue(name);
        };
        $delegate.isExperimentEnabled = function(name) {
            markAsUsedOrUnexpected(name);
            return originalIsExperimentEnabled(name);
        };
        $delegate.$$queryPetri = function(params) {
            return $q.when(params).then(resolvePromise);
        };
        $delegate.setScopeExperiments = function(str, map) {
            scopeToExperiments[str] = map;
        };
        $delegate.verifyNoUnexpectedExperiments = function() {
            if (unexpected.length) {
                throw "unexpected experiments: " + unexpected.join(", ");
            }
        };
        $delegate.verifyNoUnusedExperiments = function() {
            var unused = Object.keys($delegate.$$getExperimentsObj()).filter(function(experiment) {
                return used.indexOf(experiment) === -1;
            });
            if (unused.length) {
                throw "unused experiments: " + unused.join(", ");
            }
        };
        return $delegate;
    } ]);
} ]);

"use strict";

var PermissionsManager = function() {
    PermissionsManager.$inject = [ "provider", "wixRoles" ];
    function PermissionsManager(provider, wixRoles) {
        var _this = this;
        this.provider = provider;
        this.wixRoles = wixRoles;
        this.defaultContextKey = "default-context";
        this.defaultContextGetter = function() {
            return _this.defaultContextKey;
        };
        this.contextGetter = this.defaultContextGetter;
        this.permissionsContextMap = this.provider.permissionsContextMap || {};
        this.permissionsContextMap[this.defaultContextKey] = this.provider.permissionsDefinition;
    }
    PermissionsManager.prototype.contains = function(value, context) {
        var ctx = context || this.contextGetter();
        var permissionsDefinition = this.permissionsContextMap[ctx];
        return permissionsDefinition ? this.hasPermission(permissionsDefinition, value) : false;
    };
    PermissionsManager.prototype.get = function(value) {
        return this.contains(value).toString();
    };
    PermissionsManager.prototype.loadScope = function(scope) {
        throw new Error("This method is not implemented.");
    };
    PermissionsManager.prototype.assignPermissionsMap = function(permissionsMap) {
        angular.extend(this.permissionsContextMap, permissionsMap);
    };
    PermissionsManager.prototype.setContextGetter = function(contextGetter) {
        this.contextGetter = contextGetter;
    };
    PermissionsManager.prototype.resetContextGetter = function() {
        this.contextGetter = this.defaultContextGetter;
    };
    PermissionsManager.prototype.$$getOwnerId = function() {
        return this.getCurrentPermissionsDefinition().$$ownerId;
    };
    PermissionsManager.prototype.getSiteToken = function() {
        return this.getCurrentPermissionsDefinition().siteToken;
    };
    PermissionsManager.prototype.$$getRoles = function() {
        return this.getCurrentPermissionsDefinition().$$roles;
    };
    PermissionsManager.prototype.$$isOwner = function() {
        return this.getCurrentPermissionsDefinition().isOwner;
    };
    PermissionsManager.prototype.$$getCurrentPermissionsMap = function() {
        return this.permissionsContextMap;
    };
    PermissionsManager.prototype.isWixStaff = function(roles) {
        return roles.indexOf(this.wixRoles.WIX_STAFF) !== -1;
    };
    PermissionsManager.prototype.getCurrentPermissionsDefinition = function() {
        return this.permissionsContextMap[this.contextGetter()];
    };
    PermissionsManager.prototype.hasPermission = function(permissionsDefinition, permission) {
        var isWixStaff = this.isWixStaff(permissionsDefinition.$$roles);
        var isHavePermission = permissionsDefinition.permissions.indexOf(permission) !== -1;
        return permissionsDefinition.isOwner || isWixStaff || isHavePermission;
    };
    return PermissionsManager;
}();

var PermissionsManagerProvider = function() {
    function PermissionsManagerProvider() {
        this.permissionsDefinition = new PermissionsDefinition({
            permissions: [],
            isOwner: false,
            ownerId: undefined,
            roles: []
        });
    }
    PermissionsManagerProvider.prototype.setAuthorizationInfo = function(authorizationInfo) {
        this.permissionsDefinition = new PermissionsDefinition(authorizationInfo);
    };
    PermissionsManagerProvider.prototype.setPermissions = function(permissionsDefinition) {
        this.permissionsDefinition = permissionsDefinition;
    };
    PermissionsManagerProvider.prototype.setPermissionsContextMap = function(permissionsContextMap) {
        this.permissionsContextMap = permissionsContextMap;
    };
    PermissionsManagerProvider.prototype.$get = function($injector) {
        return $injector.instantiate(PermissionsManager, {
            provider: this
        });
    };
    PermissionsManagerProvider.prototype.$get.$inject = [ "$injector" ];
    return PermissionsManagerProvider;
}();

angular.module("wixAngularPermissions").provider("permissionsManager", PermissionsManagerProvider);

"use strict";

var IsPermitted = function() {
    IsPermitted.$inject = [ "permissionsManager", "wixManagerEval" ];
    function IsPermitted(permissionsManager, wixManagerEval) {
        this.permissionsManager = permissionsManager;
        this.wixManagerEval = wixManagerEval;
    }
    IsPermitted.prototype.filter = function(input, permissionId, fallback, context) {
        var result = this.wixManagerEval.eval(this.permissionsManager, permissionId, context);
        return result ? input : fallback;
    };
    return IsPermitted;
}();

angular.module("wixAngularPermissions").filter("isPermitted", [ "$injector", function($injector) {
    var isPermitted = $injector.instantiate(IsPermitted);
    return isPermitted.filter.bind(isPermitted);
} ]);

"use strict";

var WixManagerEval = function() {
    WixManagerEval.$inject = [ "wixConstValue" ];
    function WixManagerEval(wixConstValue) {
        this.wixConstValue = wixConstValue;
    }
    WixManagerEval.prototype.eval = function(manager, expr, context) {
        var negationGroups = /^(\!*)([^!].*)/.exec(expr);
        var negation = negationGroups[1];
        var name = negationGroups[2];
        var value = manager.contains(this.wixConstValue(name), context);
        return negation.length % 2 ? !value : value;
    };
    return WixManagerEval;
}();

angular.module("wixAngularAppInternal").service("wixManagerEval", WixManagerEval);

"use strict";

angular.module("wixAngularAppInternal").value("angularVersion", angular.version).factory("cookieReaderFacade", [ "$injector", "$browser", "angularVersion", function($injector, $browser, angularVersion) {
    if (angularVersion.minor > 3) {
        return $injector.get("$$cookieReader");
    } else {
        return function() {
            return $browser.cookies();
        };
    }
} ]);

"use strict";

(function() {
    WixDebounce.$inject = [ "$timeout" ];
    function WixDebounce($timeout) {
        return function(func, wait, immediate, invokeApply) {
            var timeout, args, context, result;
            function debounce() {
                context = this;
                args = arguments;
                var later = function() {
                    timeout = null;
                    if (!immediate) {
                        result = func.apply(context, args);
                    }
                };
                var callNow = immediate && !timeout;
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(later, wait, invokeApply);
                if (callNow) {
                    result = func.apply(context, args);
                }
                return result;
            }
            debounce.cancel = function() {
                $timeout.cancel(timeout);
                timeout = null;
            };
            return debounce;
        };
    }
    angular.module("wixAngularAppInternal").service("wixDebounce", WixDebounce);
})();

"use strict";

var WixHeightWatcherModule;

(function(WixHeightWatcherModule) {
    WixHeightWatcherModule.HEIGHT_CHANGED_EVENT = "wix-height-changed-event";
})(WixHeightWatcherModule || (WixHeightWatcherModule = {}));

var WixHeightWatcher = function() {
    WixHeightWatcher.$inject = [ "wixDebounce", "$document", "$rootScope", "$timeout" ];
    function WixHeightWatcher(wixDebounce, $document, $rootScope, $timeout) {
        var _this = this;
        this.wixDebounce = wixDebounce;
        this.$document = $document;
        this.$rootScope = $rootScope;
        this.$timeout = $timeout;
        this.height = 0;
        this.checkHeight = this.wixDebounce(this.checkHeight, 50, false, false);
        this.$rootScope.$watch(function() {
            return _this.checkHeight();
        });
    }
    WixHeightWatcher.prototype.reportHeight = function(height) {
        var _this = this;
        this.$rootScope.$apply(function() {
            return _this.$rootScope.$emit(WixHeightWatcherModule.HEIGHT_CHANGED_EVENT, height);
        });
    };
    WixHeightWatcher.prototype.checkHeight = function() {
        var document = this.$document;
        var body = document[0].body;
        var html = document[0].documentElement;
        var height = Math.max(body.offsetHeight, html.offsetHeight);
        if (this.height !== height) {
            this.height = height;
            this.reportHeight(height);
        }
    };
    WixHeightWatcher.prototype.subscribe = function(func) {
        return this.$rootScope.$on(WixHeightWatcherModule.HEIGHT_CHANGED_EVENT, function(event, height) {
            func(height);
        });
    };
    return WixHeightWatcher;
}();

angular.module("wixAngularAppInternal").service("wixHeightWatcher", WixHeightWatcher);

"use strict";

var WixTpaHeightChangedDirectiveCtrl = function() {
    WixTpaHeightChangedDirectiveCtrl.$inject = [ "$scope", "wixHeightWatcher", "$window" ];
    function WixTpaHeightChangedDirectiveCtrl($scope, wixHeightWatcher, $window) {
        var unsubscribe = wixHeightWatcher.subscribe(function(height) {
            return $window.Wix.setHeight(height);
        });
        $scope.$on("$destroy", function() {
            return unsubscribe();
        });
    }
    return WixTpaHeightChangedDirectiveCtrl;
}();

angular.module("wixAngularAppInternal").directive("wixTpaHeightChanged", function() {
    return {
        restrict: "A",
        controller: WixTpaHeightChangedDirectiveCtrl,
        controllerAs: "wixTpaHeightChangedDirectiveCtrl",
        bindToController: true
    };
});

"use strict";

var WixHeightChangedCtrl = function() {
    WixHeightChangedCtrl.$inject = [ "$scope", "$attrs", "wixHeightWatcher" ];
    function WixHeightChangedCtrl($scope, $attrs, wixHeightWatcher) {
        var unsubscribe = wixHeightWatcher.subscribe(function(height) {
            $scope.$eval($attrs.wixHeightChanged, {
                height: height
            });
        });
        $scope.$on("$destroy", function() {
            return unsubscribe();
        });
    }
    return WixHeightChangedCtrl;
}();

angular.module("wixAngularAppInternal").directive("wixHeightChanged", function() {
    return {
        controller: WixHeightChangedCtrl,
        controllerAs: "wixHeightChangedCtrl",
        bindToController: true,
        restrict: "A"
    };
});

"use strict";

angular.module("wixAngularAppInternal").run([ "$injector", "$locale", "$log", "$window", function($injector, $locale, $log, $window) {
    var translationLanguage = $injector.has("preferredLanguage") && $injector.get("preferredLanguage");
    var locale = $locale.id.slice(0, 2);
    if (translationLanguage && translationLanguage !== locale) {
        var error = "Translation language (" + translationLanguage + ") does not match locale (" + locale + ")";
        $log.error(error);
        if ($window.NREUM) {
            $window.NREUM.noticeError(new Error(error));
        }
    }
} ]);
/**
 * angular-translate - v1.1.1 - 2013-11-24
 * http://github.com/PascalPrecht/angular-translate
 * Copyright (c) 2013 ; Licensed 
 */
angular.module('pascalprecht.translate', ['ng']).run([
  '$translate',
  function ($translate) {
    var key = $translate.storageKey(), storage = $translate.storage();
    if (storage) {
      if (!storage.get(key)) {
        if (angular.isString($translate.preferredLanguage())) {
          $translate.uses($translate.preferredLanguage());
        } else {
          storage.set(key, $translate.uses());
        }
      } else {
        $translate.uses(storage.get(key));
      }
    } else if (angular.isString($translate.preferredLanguage())) {
      $translate.uses($translate.preferredLanguage());
    }
  }
]);
angular.module('pascalprecht.translate').provider('$translate', [
  '$STORAGE_KEY',
  function ($STORAGE_KEY) {
    var $translationTable = {}, $preferredLanguage, $fallbackLanguage, $uses, $nextLang, $storageFactory, $storageKey = $STORAGE_KEY, $storagePrefix, $missingTranslationHandlerFactory, $interpolationFactory, $interpolatorFactories = [], $loaderFactory, $loaderOptions, $notFoundIndicatorLeft, $notFoundIndicatorRight, NESTED_OBJECT_DELIMITER = '.';
    var translations = function (langKey, translationTable) {
      if (!langKey && !translationTable) {
        return $translationTable;
      }
      if (langKey && !translationTable) {
        if (angular.isString(langKey)) {
          return $translationTable[langKey];
        } else {
          angular.extend($translationTable, flatObject(langKey));
        }
      } else {
        if (!angular.isObject($translationTable[langKey])) {
          $translationTable[langKey] = {};
        }
        angular.extend($translationTable[langKey], flatObject(translationTable));
      }
      return this;
    };
    var flatObject = function (data, path, result, prevKey) {
      var key, keyWithPath, val;
      if (!path) {
        path = [];
      }
      if (!result) {
        result = {};
      }
      for (key in data) {
        if (!data.hasOwnProperty(key))
          continue;
        val = data[key];
        if (angular.isObject(val)) {
          flatObject(val, path.concat(key), result, key);
        } else {
          keyWithPath = path.length ? '' + path.join(NESTED_OBJECT_DELIMITER) + NESTED_OBJECT_DELIMITER + key : key;
          if (path.length && key === prevKey) {
            keyWithShortPath = '' + path.join(NESTED_OBJECT_DELIMITER);
            result[keyWithShortPath] = '@:' + keyWithPath;
          }
          result[keyWithPath] = val;
        }
      }
      return result;
    };
    this.translations = translations;
    this.addInterpolation = function (factory) {
      $interpolatorFactories.push(factory);
      return this;
    };
    this.useMessageFormatInterpolation = function () {
      return this.useInterpolation('$translateMessageFormatInterpolation');
    };
    this.useInterpolation = function (factory) {
      $interpolationFactory = factory;
      return this;
    };
    this.preferredLanguage = function (langKey) {
      if (langKey) {
        $preferredLanguage = langKey;
        return this;
      } else {
        return $preferredLanguage;
      }
    };
    this.translationNotFoundIndicator = function (indicator) {
      this.translationNotFoundIndicatorLeft(indicator);
      this.translationNotFoundIndicatorRight(indicator);
      return this;
    };
    this.translationNotFoundIndicatorLeft = function (indicator) {
      if (!indicator) {
        return $notFoundIndicatorLeft;
      }
      $notFoundIndicatorLeft = indicator;
      return this;
    };
    this.translationNotFoundIndicatorRight = function (indicator) {
      if (!indicator) {
        return $notFoundIndicatorRight;
      }
      $notFoundIndicatorRight = indicator;
      return this;
    };
    this.fallbackLanguage = function (langKey) {
      if (langKey) {
        if (typeof langKey === 'string' || angular.isArray(langKey)) {
          $fallbackLanguage = langKey;
        } else {
        }
        return this;
      } else {
        return $fallbackLanguage;
      }
    };
    this.uses = function (langKey) {
      if (langKey) {
        if (!$translationTable[langKey] && !$loaderFactory) {
          throw new Error('$translateProvider couldn\'t find translationTable for langKey: \'' + langKey + '\'');
        }
        $uses = langKey;
        return this;
      } else {
        return $uses;
      }
    };
    var storageKey = function (key) {
      if (!key) {
        if ($storagePrefix) {
          return $storagePrefix + $storageKey;
        }
        return $storageKey;
      }
      $storageKey = key;
    };
    this.storageKey = storageKey;
    this.useUrlLoader = function (url) {
      return this.useLoader('$translateUrlLoader', { url: url });
    };
    this.useStaticFilesLoader = function (options) {
      return this.useLoader('$translateStaticFilesLoader', options);
    };
    this.useLoader = function (loaderFactory, options) {
      $loaderFactory = loaderFactory;
      $loaderOptions = options || {};
      return this;
    };
    this.useLocalStorage = function () {
      return this.useStorage('$translateLocalStorage');
    };
    this.useCookieStorage = function () {
      return this.useStorage('$translateCookieStorage');
    };
    this.useStorage = function (storageFactory) {
      $storageFactory = storageFactory;
      return this;
    };
    this.storagePrefix = function (prefix) {
      if (!prefix) {
        return prefix;
      }
      $storagePrefix = prefix;
      return this;
    };
    this.useMissingTranslationHandlerLog = function () {
      return this.useMissingTranslationHandler('$translateMissingTranslationHandlerLog');
    };
    this.useMissingTranslationHandler = function (factory) {
      $missingTranslationHandlerFactory = factory;
      return this;
    };
    this.$get = [
      '$log',
      '$injector',
      '$rootScope',
      '$q',
      function ($log, $injector, $rootScope, $q) {
        var Storage, defaultInterpolator = $injector.get($interpolationFactory || '$translateDefaultInterpolation'), pendingLoader = false, interpolatorHashMap = {};
        var loadAsync = function (key) {
          if (!key) {
            throw 'No language key specified for loading.';
          }
          var deferred = $q.defer();
          $rootScope.$broadcast('$translateLoadingStart');
          pendingLoader = true;
          $injector.get($loaderFactory)(angular.extend($loaderOptions, { key: key })).then(function (data) {
            $rootScope.$broadcast('$translateLoadingSuccess');
            var translationTable = {};
            if (angular.isArray(data)) {
              angular.forEach(data, function (table) {
                angular.extend(translationTable, table);
              });
            } else {
              angular.extend(translationTable, data);
            }
            pendingLoader = false;
            deferred.resolve({
              key: key,
              table: translationTable
            });
            $rootScope.$broadcast('$translateLoadingEnd');
          }, function (key) {
            $rootScope.$broadcast('$translateLoadingError');
            deferred.reject(key);
            $rootScope.$broadcast('$translateLoadingEnd');
          });
          return deferred.promise;
        };
        if ($storageFactory) {
          Storage = $injector.get($storageFactory);
          if (!Storage.get || !Storage.set) {
            throw new Error('Couldn\'t use storage \'' + $storageFactory + '\', missing get() or set() method!');
          }
        }
        if ($interpolatorFactories.length > 0) {
          angular.forEach($interpolatorFactories, function (interpolatorFactory) {
            var interpolator = $injector.get(interpolatorFactory);
            interpolator.setLocale($preferredLanguage || $uses);
            interpolatorHashMap[interpolator.getInterpolationIdentifier()] = interpolator;
          });
        }
        var checkValidFallback = function (usesLang) {
          if (usesLang && $fallbackLanguage) {
            if (angular.isArray($fallbackLanguage)) {
              var fallbackLanguagesSize = $fallbackLanguage.length;
              for (var current = 0; current < fallbackLanguagesSize; current++) {
                if ($uses === $translationTable[$fallbackLanguage[current]]) {
                  return false;
                }
              }
              return true;
            } else {
              return usesLang !== $fallbackLanguage;
            }
          } else {
            return false;
          }
          return false;
        };
        var $translate = function (translationId, interpolateParams, interpolationId) {
          var table = $uses ? $translationTable[$uses] : $translationTable, Interpolator = interpolationId ? interpolatorHashMap[interpolationId] : defaultInterpolator;
          if (table && table.hasOwnProperty(translationId)) {
            if (angular.isString(table[translationId]) && table[translationId].substr(0, 2) === '@:') {
              return $translate(table[translationId].substr(2), interpolateParams, interpolationId);
            }
            return Interpolator.interpolate(table[translationId], interpolateParams);
          }
          if ($missingTranslationHandlerFactory && !pendingLoader) {
            $injector.get($missingTranslationHandlerFactory)(translationId, $uses);
          }
          var normatedLanguages;
          if ($uses && $fallbackLanguage && checkValidFallback($uses)) {
            if (typeof $fallbackLanguage === 'string') {
              normatedLanguages = [];
              normatedLanguages.push($fallbackLanguage);
            } else {
              normatedLanguages = $fallbackLanguage;
            }
            var fallbackLanguagesSize = normatedLanguages.length;
            for (var current = 0; current < fallbackLanguagesSize; current++) {
              if ($uses !== $translationTable[normatedLanguages[current]]) {
                var translationFromList = $translationTable[normatedLanguages[current]][translationId];
                if (translationFromList) {
                  var returnValFromList;
                  Interpolator.setLocale(normatedLanguages[current]);
                  returnValFromList = Interpolator.interpolate(translationFromList, interpolateParams);
                  Interpolator.setLocale($uses);
                  return returnValFromList;
                }
              }
            }
          }
          if ($notFoundIndicatorLeft) {
            translationId = [
              $notFoundIndicatorLeft,
              translationId
            ].join(' ');
          }
          if ($notFoundIndicatorRight) {
            translationId = [
              translationId,
              $notFoundIndicatorRight
            ].join(' ');
          }
          return translationId;
        };
        $translate.preferredLanguage = function () {
          return $preferredLanguage;
        };
        $translate.fallbackLanguage = function () {
          return $fallbackLanguage;
        };
        $translate.proposedLanguage = function () {
          return $nextLang;
        };
        $translate.storage = function () {
          return Storage;
        };
        $translate.uses = function (key) {
          if (!key) {
            return $uses;
          }
          var deferred = $q.defer();
          $rootScope.$broadcast('$translateChangeStart');
          function useLanguage(key) {
            $uses = key;
            $rootScope.$broadcast('$translateChangeSuccess');
            if ($storageFactory) {
              Storage.set($translate.storageKey(), $uses);
            }
            defaultInterpolator.setLocale($uses);
            angular.forEach(interpolatorHashMap, function (interpolator, id) {
              interpolatorHashMap[id].setLocale($uses);
            });
            deferred.resolve(key);
            $rootScope.$broadcast('$translateChangeEnd');
          }
          if (!$translationTable[key] && $loaderFactory) {
            $nextLang = key;
            loadAsync(key).then(function (translation) {
              $nextLang = undefined;
              translations(translation.key, translation.table);
              useLanguage(translation.key);
            }, function (key) {
              $nextLang = undefined;
              $rootScope.$broadcast('$translateChangeError');
              deferred.reject(key);
              $rootScope.$broadcast('$translateChangeEnd');
            });
          } else {
            useLanguage(key);
          }
          return deferred.promise;
        };
        $translate.storageKey = function () {
          return storageKey();
        };
        $translate.refresh = function (langKey) {
          if (!$loaderFactory) {
            throw new Error('Couldn\'t refresh translation table, no loader registered!');
          }
          var deferred = $q.defer();
          function onLoadSuccess() {
            deferred.resolve();
            $rootScope.$broadcast('$translateRefreshEnd');
          }
          function onLoadFailure() {
            deferred.reject();
            $rootScope.$broadcast('$translateRefreshEnd');
          }
          if (!langKey) {
            $rootScope.$broadcast('$translateRefreshStart');
            var loaders = [];
            if ($fallbackLanguage) {
              if (typeof $fallbackLanguage === 'string') {
                loaders.push(loadAsync($fallbackLanguage));
              } else {
                var fallbackLanguagesSize = $fallbackLanguage.length;
                for (var current = 0; current < fallbackLanguagesSize; current++) {
                  loaders.push(loadAsync($fallbackLanguage[current]));
                }
              }
            }
            if ($uses) {
              loaders.push(loadAsync($uses));
            }
            if (loaders.length > 0) {
              $q.all(loaders).then(function (newTranslations) {
                for (var lang in $translationTable) {
                  if ($translationTable.hasOwnProperty(lang)) {
                    delete $translationTable[lang];
                  }
                }
                for (var i = 0, len = newTranslations.length; i < len; i++) {
                  translations(newTranslations[i].key, newTranslations[i].table);
                }
                if ($uses) {
                  $translate.uses($uses);
                }
                onLoadSuccess();
              }, function (key) {
                if (key === $uses) {
                  $rootScope.$broadcast('$translateChangeError');
                }
                onLoadFailure();
              });
            } else
              onLoadSuccess();
          } else if ($translationTable.hasOwnProperty(langKey)) {
            $rootScope.$broadcast('$translateRefreshStart');
            var loader = loadAsync(langKey);
            if (langKey === $uses) {
              loader.then(function (newTranslation) {
                $translationTable[langKey] = newTranslation.table;
                $translate.uses($uses);
                onLoadSuccess();
              }, function () {
                $rootScope.$broadcast('$translateChangeError');
                onLoadFailure();
              });
            } else {
              loader.then(function (newTranslation) {
                $translationTable[langKey] = newTranslation.table;
                onLoadSuccess();
              }, onLoadFailure);
            }
          } else
            deferred.reject();
          return deferred.promise;
        };
        if ($loaderFactory) {
          if (angular.equals($translationTable, {})) {
            $translate.uses($translate.uses());
          }
          if ($fallbackLanguage) {
            if (typeof $fallbackLanguage === 'string' && !$translationTable[$fallbackLanguage]) {
              loadAsync($fallbackLanguage);
            } else {
              var fallbackLanguagesSize = $fallbackLanguage.length;
              for (var current = 0; current < fallbackLanguagesSize; current++) {
                if (!$translationTable[$fallbackLanguage[current]]) {
                  loadAsync($fallbackLanguage[current]);
                }
              }
            }
          }
        }
        return $translate;
      }
    ];
  }
]);
angular.module('pascalprecht.translate').factory('$translateDefaultInterpolation', [
  '$interpolate',
  function ($interpolate) {
    var $translateInterpolator = {}, $locale, $identifier = 'default';
    $translateInterpolator.setLocale = function (locale) {
      $locale = locale;
    };
    $translateInterpolator.getInterpolationIdentifier = function () {
      return $identifier;
    };
    $translateInterpolator.interpolate = function (string, interpolateParams) {
      return $interpolate(string)(interpolateParams);
    };
    return $translateInterpolator;
  }
]);
angular.module('pascalprecht.translate').constant('$STORAGE_KEY', 'NG_TRANSLATE_LANG_KEY');
angular.module('pascalprecht.translate').directive('translate', [
  '$filter',
  '$interpolate',
  '$parse',
  function ($filter, $interpolate, $parse) {
    var translate = $filter('translate');
    return {
      restrict: 'AE',
      scope: true,
      link: function linkFn(scope, element, attr) {
        if (attr.translateInterpolation) {
          scope.interpolation = attr.translateInterpolation;
        }
        attr.$observe('translate', function (translationId) {
          if (angular.equals(translationId, '') || translationId === undefined) {
            scope.translationId = $interpolate(element.text().replace(/^\s+|\s+$/g, ''))(scope.$parent);
          } else {
            scope.translationId = translationId;
          }
        });
        attr.$observe('translateValues', function (interpolateParams) {
          if (interpolateParams)
            scope.$parent.$watch(function () {
              scope.interpolateParams = $parse(interpolateParams)(scope.$parent);
            });
        });
        scope.$on('$translateChangeSuccess', function () {
          element.html(translate(scope.translationId, scope.interpolateParams, scope.interpolation));
        });
        scope.$watch('[translationId, interpolateParams]', function (nValue) {
          if (scope.translationId) {
            element.html(translate(scope.translationId, scope.interpolateParams, scope.interpolation));
          }
        }, true);
      }
    };
  }
]);
angular.module('pascalprecht.translate').filter('translate', [
  '$parse',
  '$translate',
  function ($parse, $translate) {
    return function (translationId, interpolateParams, interpolation) {
      if (!angular.isObject(interpolateParams)) {
        interpolateParams = $parse(interpolateParams)();
      }
      return $translate(translationId, interpolateParams, interpolation);
    };
  }
]);
/**
 * @license AngularJS v1.2.30
 * (c) 2010-2014 Google, Inc. http://angularjs.org
 * License: MIT
 */
(function(window, angular, undefined) {'use strict';

var $resourceMinErr = angular.$$minErr('$resource');

// Helper functions and regex to lookup a dotted path on an object
// stopping at undefined/null.  The path must be composed of ASCII
// identifiers (just like $parse)
var MEMBER_NAME_REGEX = /^(\.[a-zA-Z_$][0-9a-zA-Z_$]*)+$/;

function isValidDottedPath(path) {
  return (path != null && path !== '' && path !== 'hasOwnProperty' &&
      MEMBER_NAME_REGEX.test('.' + path));
}

function lookupDottedPath(obj, path) {
  if (!isValidDottedPath(path)) {
    throw $resourceMinErr('badmember', 'Dotted member path "@{0}" is invalid.', path);
  }
  var keys = path.split('.');
  for (var i = 0, ii = keys.length; i < ii && obj !== undefined; i++) {
    var key = keys[i];
    obj = (obj !== null) ? obj[key] : undefined;
  }
  return obj;
}

/**
 * Create a shallow copy of an object and clear other fields from the destination
 */
function shallowClearAndCopy(src, dst) {
  dst = dst || {};

  angular.forEach(dst, function(value, key){
    delete dst[key];
  });

  for (var key in src) {
    if (src.hasOwnProperty(key) && !(key.charAt(0) === '$' && key.charAt(1) === '$')) {
      dst[key] = src[key];
    }
  }

  return dst;
}

/**
 * @ngdoc module
 * @name ngResource
 * @description
 *
 * # ngResource
 *
 * The `ngResource` module provides interaction support with RESTful services
 * via the $resource service.
 *
 *
 * <div doc-module-components="ngResource"></div>
 *
 * See {@link ngResource.$resource `$resource`} for usage.
 */

/**
 * @ngdoc service
 * @name $resource
 * @requires $http
 *
 * @description
 * A factory which creates a resource object that lets you interact with
 * [RESTful](http://en.wikipedia.org/wiki/Representational_State_Transfer) server-side data sources.
 *
 * The returned resource object has action methods which provide high-level behaviors without
 * the need to interact with the low level {@link ng.$http $http} service.
 *
 * Requires the {@link ngResource `ngResource`} module to be installed.
 *
 * @param {string} url A parametrized URL template with parameters prefixed by `:` as in
 *   `/user/:username`. If you are using a URL with a port number (e.g.
 *   `http://example.com:8080/api`), it will be respected.
 *
 *   If you are using a url with a suffix, just add the suffix, like this:
 *   `$resource('http://example.com/resource.json')` or `$resource('http://example.com/:id.json')`
 *   or even `$resource('http://example.com/resource/:resource_id.:format')`
 *   If the parameter before the suffix is empty, :resource_id in this case, then the `/.` will be
 *   collapsed down to a single `.`.  If you need this sequence to appear and not collapse then you
 *   can escape it with `/\.`.
 *
 * @param {Object=} paramDefaults Default values for `url` parameters. These can be overridden in
 *   `actions` methods. If any of the parameter value is a function, it will be executed every time
 *   when a param value needs to be obtained for a request (unless the param was overridden).
 *
 *   Each key value in the parameter object is first bound to url template if present and then any
 *   excess keys are appended to the url search query after the `?`.
 *
 *   Given a template `/path/:verb` and parameter `{verb:'greet', salutation:'Hello'}` results in
 *   URL `/path/greet?salutation=Hello`.
 *
 *   If the parameter value is prefixed with `@` then the value for that parameter will be extracted
 *   from the corresponding property on the `data` object (provided when calling an action method).  For
 *   example, if the `defaultParam` object is `{someParam: '@someProp'}` then the value of `someParam`
 *   will be `data.someProp`.
 *
 * @param {Object.<Object>=} actions Hash with declaration of custom action that should extend
 *   the default set of resource actions. The declaration should be created in the format of {@link
 *   ng.$http#usage_parameters $http.config}:
 *
 *       {action1: {method:?, params:?, isArray:?, headers:?, ...},
 *        action2: {method:?, params:?, isArray:?, headers:?, ...},
 *        ...}
 *
 *   Where:
 *
 *   - **`action`** – {string} – The name of action. This name becomes the name of the method on
 *     your resource object.
 *   - **`method`** – {string} – Case insensitive HTTP method (e.g. `GET`, `POST`, `PUT`,
 *     `DELETE`, `JSONP`, etc).
 *   - **`params`** – {Object=} – Optional set of pre-bound parameters for this action. If any of
 *     the parameter value is a function, it will be executed every time when a param value needs to
 *     be obtained for a request (unless the param was overridden).
 *   - **`url`** – {string} – action specific `url` override. The url templating is supported just
 *     like for the resource-level urls.
 *   - **`isArray`** – {boolean=} – If true then the returned object for this action is an array,
 *     see `returns` section.
 *   - **`transformRequest`** –
 *     `{function(data, headersGetter)|Array.<function(data, headersGetter)>}` –
 *     transform function or an array of such functions. The transform function takes the http
 *     request body and headers and returns its transformed (typically serialized) version.
 *     By default, transformRequest will contain one function that checks if the request data is
 *     an object and serializes to using `angular.toJson`. To prevent this behavior, set
 *     `transformRequest` to an empty array: `transformRequest: []`
 *   - **`transformResponse`** –
 *     `{function(data, headersGetter)|Array.<function(data, headersGetter)>}` –
 *     transform function or an array of such functions. The transform function takes the http
 *     response body and headers and returns its transformed (typically deserialized) version.
 *     By default, transformResponse will contain one function that checks if the response looks like
 *     a JSON string and deserializes it using `angular.fromJson`. To prevent this behavior, set
 *     `transformResponse` to an empty array: `transformResponse: []`
 *   - **`cache`** – `{boolean|Cache}` – If true, a default $http cache will be used to cache the
 *     GET request, otherwise if a cache instance built with
 *     {@link ng.$cacheFactory $cacheFactory}, this cache will be used for
 *     caching.
 *   - **`timeout`** – `{number|Promise}` – timeout in milliseconds, or {@link ng.$q promise} that
 *     should abort the request when resolved.
 *   - **`withCredentials`** - `{boolean}` - whether to set the `withCredentials` flag on the
 *     XHR object. See
 *     [requests with credentials](https://developer.mozilla.org/en/http_access_control#section_5)
 *     for more information.
 *   - **`responseType`** - `{string}` - see
 *     [requestType](https://developer.mozilla.org/en-US/docs/DOM/XMLHttpRequest#responseType).
 *   - **`interceptor`** - `{Object=}` - The interceptor object has two optional methods -
 *     `response` and `responseError`. Both `response` and `responseError` interceptors get called
 *     with `http response` object. See {@link ng.$http $http interceptors}.
 *
 * @returns {Object} A resource "class" object with methods for the default set of resource actions
 *   optionally extended with custom `actions`. The default set contains these actions:
 *   ```js
 *   { 'get':    {method:'GET'},
 *     'save':   {method:'POST'},
 *     'query':  {method:'GET', isArray:true},
 *     'remove': {method:'DELETE'},
 *     'delete': {method:'DELETE'} };
 *   ```
 *
 *   Calling these methods invoke an {@link ng.$http} with the specified http method,
 *   destination and parameters. When the data is returned from the server then the object is an
 *   instance of the resource class. The actions `save`, `remove` and `delete` are available on it
 *   as  methods with the `$` prefix. This allows you to easily perform CRUD operations (create,
 *   read, update, delete) on server-side data like this:
 *   ```js
 *   var User = $resource('/user/:userId', {userId:'@id'});
 *   var user = User.get({userId:123}, function() {
 *     user.abc = true;
 *     user.$save();
 *   });
 *   ```
 *
 *   It is important to realize that invoking a $resource object method immediately returns an
 *   empty reference (object or array depending on `isArray`). Once the data is returned from the
 *   server the existing reference is populated with the actual data. This is a useful trick since
 *   usually the resource is assigned to a model which is then rendered by the view. Having an empty
 *   object results in no rendering, once the data arrives from the server then the object is
 *   populated with the data and the view automatically re-renders itself showing the new data. This
 *   means that in most cases one never has to write a callback function for the action methods.
 *
 *   The action methods on the class object or instance object can be invoked with the following
 *   parameters:
 *
 *   - HTTP GET "class" actions: `Resource.action([parameters], [success], [error])`
 *   - non-GET "class" actions: `Resource.action([parameters], postData, [success], [error])`
 *   - non-GET instance actions:  `instance.$action([parameters], [success], [error])`
 *
 *   Success callback is called with (value, responseHeaders) arguments. Error callback is called
 *   with (httpResponse) argument.
 *
 *   Class actions return empty instance (with additional properties below).
 *   Instance actions return promise of the action.
 *
 *   The Resource instances and collection have these additional properties:
 *
 *   - `$promise`: the {@link ng.$q promise} of the original server interaction that created this
 *     instance or collection.
 *
 *     On success, the promise is resolved with the same resource instance or collection object,
 *     updated with data from server. This makes it easy to use in
 *     {@link ngRoute.$routeProvider resolve section of $routeProvider.when()} to defer view
 *     rendering until the resource(s) are loaded.
 *
 *     On failure, the promise is resolved with the {@link ng.$http http response} object, without
 *     the `resource` property.
 *
 *     If an interceptor object was provided, the promise will instead be resolved with the value
 *     returned by the interceptor.
 *
 *   - `$resolved`: `true` after first server interaction is completed (either with success or
 *      rejection), `false` before that. Knowing if the Resource has been resolved is useful in
 *      data-binding.
 *
 * @example
 *
 * # Credit card resource
 *
 * ```js
     // Define CreditCard class
     var CreditCard = $resource('/user/:userId/card/:cardId',
      {userId:123, cardId:'@id'}, {
       charge: {method:'POST', params:{charge:true}}
      });

     // We can retrieve a collection from the server
     var cards = CreditCard.query(function() {
       // GET: /user/123/card
       // server returns: [ {id:456, number:'1234', name:'Smith'} ];

       var card = cards[0];
       // each item is an instance of CreditCard
       expect(card instanceof CreditCard).toEqual(true);
       card.name = "J. Smith";
       // non GET methods are mapped onto the instances
       card.$save();
       // POST: /user/123/card/456 {id:456, number:'1234', name:'J. Smith'}
       // server returns: {id:456, number:'1234', name: 'J. Smith'};

       // our custom method is mapped as well.
       card.$charge({amount:9.99});
       // POST: /user/123/card/456?amount=9.99&charge=true {id:456, number:'1234', name:'J. Smith'}
     });

     // we can create an instance as well
     var newCard = new CreditCard({number:'0123'});
     newCard.name = "Mike Smith";
     newCard.$save();
     // POST: /user/123/card {number:'0123', name:'Mike Smith'}
     // server returns: {id:789, number:'0123', name: 'Mike Smith'};
     expect(newCard.id).toEqual(789);
 * ```
 *
 * The object returned from this function execution is a resource "class" which has "static" method
 * for each action in the definition.
 *
 * Calling these methods invoke `$http` on the `url` template with the given `method`, `params` and
 * `headers`.
 * When the data is returned from the server then the object is an instance of the resource type and
 * all of the non-GET methods are available with `$` prefix. This allows you to easily support CRUD
 * operations (create, read, update, delete) on server-side data.

   ```js
     var User = $resource('/user/:userId', {userId:'@id'});
     User.get({userId:123}, function(user) {
       user.abc = true;
       user.$save();
     });
   ```
 *
 * It's worth noting that the success callback for `get`, `query` and other methods gets passed
 * in the response that came from the server as well as $http header getter function, so one
 * could rewrite the above example and get access to http headers as:
 *
   ```js
     var User = $resource('/user/:userId', {userId:'@id'});
     User.get({userId:123}, function(u, getResponseHeaders){
       u.abc = true;
       u.$save(function(u, putResponseHeaders) {
         //u => saved user object
         //putResponseHeaders => $http header getter
       });
     });
   ```
 *
 * You can also access the raw `$http` promise via the `$promise` property on the object returned
 *
   ```
     var User = $resource('/user/:userId', {userId:'@id'});
     User.get({userId:123})
         .$promise.then(function(user) {
           $scope.user = user;
         });
   ```

 * # Creating a custom 'PUT' request
 * In this example we create a custom method on our resource to make a PUT request
 * ```js
 *    var app = angular.module('app', ['ngResource', 'ngRoute']);
 *
 *    // Some APIs expect a PUT request in the format URL/object/ID
 *    // Here we are creating an 'update' method
 *    app.factory('Notes', ['$resource', function($resource) {
 *    return $resource('/notes/:id', null,
 *        {
 *            'update': { method:'PUT' }
 *        });
 *    }]);
 *
 *    // In our controller we get the ID from the URL using ngRoute and $routeParams
 *    // We pass in $routeParams and our Notes factory along with $scope
 *    app.controller('NotesCtrl', ['$scope', '$routeParams', 'Notes',
                                      function($scope, $routeParams, Notes) {
 *    // First get a note object from the factory
 *    var note = Notes.get({ id:$routeParams.id });
 *    $id = note.id;
 *
 *    // Now call update passing in the ID first then the object you are updating
 *    Notes.update({ id:$id }, note);
 *
 *    // This will PUT /notes/ID with the note object in the request payload
 *    }]);
 * ```
 */
angular.module('ngResource', ['ng']).
  factory('$resource', ['$http', '$q', function($http, $q) {

    var DEFAULT_ACTIONS = {
      'get':    {method:'GET'},
      'save':   {method:'POST'},
      'query':  {method:'GET', isArray:true},
      'remove': {method:'DELETE'},
      'delete': {method:'DELETE'}
    };
    var noop = angular.noop,
        forEach = angular.forEach,
        extend = angular.extend,
        copy = angular.copy,
        isFunction = angular.isFunction;

    /**
     * We need our custom method because encodeURIComponent is too aggressive and doesn't follow
     * http://www.ietf.org/rfc/rfc3986.txt with regards to the character set (pchar) allowed in path
     * segments:
     *    segment       = *pchar
     *    pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
     *    pct-encoded   = "%" HEXDIG HEXDIG
     *    unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
     *    sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
     *                     / "*" / "+" / "," / ";" / "="
     */
    function encodeUriSegment(val) {
      return encodeUriQuery(val, true).
        replace(/%26/gi, '&').
        replace(/%3D/gi, '=').
        replace(/%2B/gi, '+');
    }


    /**
     * This method is intended for encoding *key* or *value* parts of query component. We need a
     * custom method because encodeURIComponent is too aggressive and encodes stuff that doesn't
     * have to be encoded per http://tools.ietf.org/html/rfc3986:
     *    query       = *( pchar / "/" / "?" )
     *    pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
     *    unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
     *    pct-encoded   = "%" HEXDIG HEXDIG
     *    sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
     *                     / "*" / "+" / "," / ";" / "="
     */
    function encodeUriQuery(val, pctEncodeSpaces) {
      return encodeURIComponent(val).
        replace(/%40/gi, '@').
        replace(/%3A/gi, ':').
        replace(/%24/g, '$').
        replace(/%2C/gi, ',').
        replace(/%20/g, (pctEncodeSpaces ? '%20' : '+'));
    }

    function Route(template, defaults) {
      this.template = template;
      this.defaults = defaults || {};
      this.urlParams = {};
    }

    Route.prototype = {
      setUrlParams: function(config, params, actionUrl) {
        var self = this,
            url = actionUrl || self.template,
            val,
            encodedVal;

        var urlParams = self.urlParams = {};
        forEach(url.split(/\W/), function(param){
          if (param === 'hasOwnProperty') {
            throw $resourceMinErr('badname', "hasOwnProperty is not a valid parameter name.");
          }
          if (!(new RegExp("^\\d+$").test(param)) && param &&
               (new RegExp("(^|[^\\\\]):" + param + "(\\W|$)").test(url))) {
            urlParams[param] = true;
          }
        });
        url = url.replace(/\\:/g, ':');

        params = params || {};
        forEach(self.urlParams, function(_, urlParam){
          val = params.hasOwnProperty(urlParam) ? params[urlParam] : self.defaults[urlParam];
          if (angular.isDefined(val) && val !== null) {
            encodedVal = encodeUriSegment(val);
            url = url.replace(new RegExp(":" + urlParam + "(\\W|$)", "g"), function(match, p1) {
              return encodedVal + p1;
            });
          } else {
            url = url.replace(new RegExp("(\/?):" + urlParam + "(\\W|$)", "g"), function(match,
                leadingSlashes, tail) {
              if (tail.charAt(0) == '/') {
                return tail;
              } else {
                return leadingSlashes + tail;
              }
            });
          }
        });

        // strip trailing slashes and set the url
        url = url.replace(/\/+$/, '') || '/';
        // then replace collapse `/.` if found in the last URL path segment before the query
        // E.g. `http://url.com/id./format?q=x` becomes `http://url.com/id.format?q=x`
        url = url.replace(/\/\.(?=\w+($|\?))/, '.');
        // replace escaped `/\.` with `/.`
        config.url = url.replace(/\/\\\./, '/.');


        // set params - delegate param encoding to $http
        forEach(params, function(value, key){
          if (!self.urlParams[key]) {
            config.params = config.params || {};
            config.params[key] = value;
          }
        });
      }
    };


    function resourceFactory(url, paramDefaults, actions) {
      var route = new Route(url);

      actions = extend({}, DEFAULT_ACTIONS, actions);

      function extractParams(data, actionParams){
        var ids = {};
        actionParams = extend({}, paramDefaults, actionParams);
        forEach(actionParams, function(value, key){
          if (isFunction(value)) { value = value(); }
          ids[key] = value && value.charAt && value.charAt(0) == '@' ?
            lookupDottedPath(data, value.substr(1)) : value;
        });
        return ids;
      }

      function defaultResponseInterceptor(response) {
        return response.resource;
      }

      function Resource(value){
        shallowClearAndCopy(value || {}, this);
      }

      forEach(actions, function(action, name) {
        var hasBody = /^(POST|PUT|PATCH)$/i.test(action.method);

        Resource[name] = function(a1, a2, a3, a4) {
          var params = {}, data, success, error;

          /* jshint -W086 */ /* (purposefully fall through case statements) */
          switch(arguments.length) {
          case 4:
            error = a4;
            success = a3;
            //fallthrough
          case 3:
          case 2:
            if (isFunction(a2)) {
              if (isFunction(a1)) {
                success = a1;
                error = a2;
                break;
              }

              success = a2;
              error = a3;
              //fallthrough
            } else {
              params = a1;
              data = a2;
              success = a3;
              break;
            }
          case 1:
            if (isFunction(a1)) success = a1;
            else if (hasBody) data = a1;
            else params = a1;
            break;
          case 0: break;
          default:
            throw $resourceMinErr('badargs',
              "Expected up to 4 arguments [params, data, success, error], got {0} arguments",
              arguments.length);
          }
          /* jshint +W086 */ /* (purposefully fall through case statements) */

          var isInstanceCall = this instanceof Resource;
          var value = isInstanceCall ? data : (action.isArray ? [] : new Resource(data));
          var httpConfig = {};
          var responseInterceptor = action.interceptor && action.interceptor.response ||
                                    defaultResponseInterceptor;
          var responseErrorInterceptor = action.interceptor && action.interceptor.responseError ||
                                    undefined;

          forEach(action, function(value, key) {
            if (key != 'params' && key != 'isArray' && key != 'interceptor') {
              httpConfig[key] = copy(value);
            }
          });

          if (hasBody) httpConfig.data = data;
          route.setUrlParams(httpConfig,
                             extend({}, extractParams(data, action.params || {}), params),
                             action.url);

          var promise = $http(httpConfig).then(function (response) {
            var data = response.data,
              promise = value.$promise;

            if (data) {
              // Need to convert action.isArray to boolean in case it is undefined
              // jshint -W018
              if (angular.isArray(data) !== (!!action.isArray)) {
                throw $resourceMinErr('badcfg',
                    'Error in resource configuration. Expected ' +
                    'response to contain an {0} but got an {1}',
                  action.isArray ? 'array' : 'object',
                  angular.isArray(data) ? 'array' : 'object');
              }
              // jshint +W018
              if (action.isArray) {
                value.length = 0;
                forEach(data, function (item) {
                  if (typeof item === "object") {
                    value.push(new Resource(item));
                  } else {
                    // Valid JSON values may be string literals, and these should not be converted
                    // into objects. These items will not have access to the Resource prototype
                    // methods, but unfortunately there
                    value.push(item);
                  }
                });
              } else {
                shallowClearAndCopy(data, value);
                value.$promise = promise;
              }
            }

            value.$resolved = true;

            response.resource = value;

            return response;
          }, function(response) {
            value.$resolved = true;

            (error||noop)(response);

            return $q.reject(response);
          });

          promise = promise.then(
              function(response) {
                var value = responseInterceptor(response);
                (success||noop)(value, response.headers);
                return value;
              },
              responseErrorInterceptor);

          if (!isInstanceCall) {
            // we are creating instance / collection
            // - set the initial promise
            // - return the instance / collection
            value.$promise = promise;
            value.$resolved = false;

            return value;
          }

          // instance call
          return promise;
        };


        Resource.prototype['$' + name] = function(params, success, error) {
          if (isFunction(params)) {
            error = success; success = params; params = {};
          }
          var result = Resource[name].call(this, params, this, success, error);
          return result.$promise || result;
        };
      });

      Resource.bind = function(additionalParamDefaults){
        return resourceFactory(url, extend({}, paramDefaults, additionalParamDefaults), actions);
      };

      return Resource;
    }

    return resourceFactory;
  }]);


})(window, window.angular);

//     Underscore.js 1.5.2
//     http://underscorejs.org
//     (c) 2009-2013 Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
//     Underscore may be freely distributed under the MIT license.

(function() {

  // Baseline setup
  // --------------

  // Establish the root object, `window` in the browser, or `exports` on the server.
  var root = this;

  // Save the previous value of the `_` variable.
  var previousUnderscore = root._;

  // Establish the object that gets returned to break out of a loop iteration.
  var breaker = {};

  // Save bytes in the minified (but not gzipped) version:
  var ArrayProto = Array.prototype, ObjProto = Object.prototype, FuncProto = Function.prototype;

  // Create quick reference variables for speed access to core prototypes.
  var
    push             = ArrayProto.push,
    slice            = ArrayProto.slice,
    concat           = ArrayProto.concat,
    toString         = ObjProto.toString,
    hasOwnProperty   = ObjProto.hasOwnProperty;

  // All **ECMAScript 5** native function implementations that we hope to use
  // are declared here.
  var
    nativeForEach      = ArrayProto.forEach,
    nativeMap          = ArrayProto.map,
    nativeReduce       = ArrayProto.reduce,
    nativeReduceRight  = ArrayProto.reduceRight,
    nativeFilter       = ArrayProto.filter,
    nativeEvery        = ArrayProto.every,
    nativeSome         = ArrayProto.some,
    nativeIndexOf      = ArrayProto.indexOf,
    nativeLastIndexOf  = ArrayProto.lastIndexOf,
    nativeIsArray      = Array.isArray,
    nativeKeys         = Object.keys,
    nativeBind         = FuncProto.bind;

  // Create a safe reference to the Underscore object for use below.
  var _ = function(obj) {
    if (obj instanceof _) return obj;
    if (!(this instanceof _)) return new _(obj);
    this._wrapped = obj;
  };

  // Export the Underscore object for **Node.js**, with
  // backwards-compatibility for the old `require()` API. If we're in
  // the browser, add `_` as a global object via a string identifier,
  // for Closure Compiler "advanced" mode.
  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = _;
    }
    exports._ = _;
  } else {
    root._ = _;
  }

  // Current version.
  _.VERSION = '1.5.2';

  // Collection Functions
  // --------------------

  // The cornerstone, an `each` implementation, aka `forEach`.
  // Handles objects with the built-in `forEach`, arrays, and raw objects.
  // Delegates to **ECMAScript 5**'s native `forEach` if available.
  var each = _.each = _.forEach = function(obj, iterator, context) {
    if (obj == null) return;
    if (nativeForEach && obj.forEach === nativeForEach) {
      obj.forEach(iterator, context);
    } else if (obj.length === +obj.length) {
      for (var i = 0, length = obj.length; i < length; i++) {
        if (iterator.call(context, obj[i], i, obj) === breaker) return;
      }
    } else {
      var keys = _.keys(obj);
      for (var i = 0, length = keys.length; i < length; i++) {
        if (iterator.call(context, obj[keys[i]], keys[i], obj) === breaker) return;
      }
    }
  };

  // Return the results of applying the iterator to each element.
  // Delegates to **ECMAScript 5**'s native `map` if available.
  _.map = _.collect = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    if (nativeMap && obj.map === nativeMap) return obj.map(iterator, context);
    each(obj, function(value, index, list) {
      results.push(iterator.call(context, value, index, list));
    });
    return results;
  };

  var reduceError = 'Reduce of empty array with no initial value';

  // **Reduce** builds up a single result from a list of values, aka `inject`,
  // or `foldl`. Delegates to **ECMAScript 5**'s native `reduce` if available.
  _.reduce = _.foldl = _.inject = function(obj, iterator, memo, context) {
    var initial = arguments.length > 2;
    if (obj == null) obj = [];
    if (nativeReduce && obj.reduce === nativeReduce) {
      if (context) iterator = _.bind(iterator, context);
      return initial ? obj.reduce(iterator, memo) : obj.reduce(iterator);
    }
    each(obj, function(value, index, list) {
      if (!initial) {
        memo = value;
        initial = true;
      } else {
        memo = iterator.call(context, memo, value, index, list);
      }
    });
    if (!initial) throw new TypeError(reduceError);
    return memo;
  };

  // The right-associative version of reduce, also known as `foldr`.
  // Delegates to **ECMAScript 5**'s native `reduceRight` if available.
  _.reduceRight = _.foldr = function(obj, iterator, memo, context) {
    var initial = arguments.length > 2;
    if (obj == null) obj = [];
    if (nativeReduceRight && obj.reduceRight === nativeReduceRight) {
      if (context) iterator = _.bind(iterator, context);
      return initial ? obj.reduceRight(iterator, memo) : obj.reduceRight(iterator);
    }
    var length = obj.length;
    if (length !== +length) {
      var keys = _.keys(obj);
      length = keys.length;
    }
    each(obj, function(value, index, list) {
      index = keys ? keys[--length] : --length;
      if (!initial) {
        memo = obj[index];
        initial = true;
      } else {
        memo = iterator.call(context, memo, obj[index], index, list);
      }
    });
    if (!initial) throw new TypeError(reduceError);
    return memo;
  };

  // Return the first value which passes a truth test. Aliased as `detect`.
  _.find = _.detect = function(obj, iterator, context) {
    var result;
    any(obj, function(value, index, list) {
      if (iterator.call(context, value, index, list)) {
        result = value;
        return true;
      }
    });
    return result;
  };

  // Return all the elements that pass a truth test.
  // Delegates to **ECMAScript 5**'s native `filter` if available.
  // Aliased as `select`.
  _.filter = _.select = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    if (nativeFilter && obj.filter === nativeFilter) return obj.filter(iterator, context);
    each(obj, function(value, index, list) {
      if (iterator.call(context, value, index, list)) results.push(value);
    });
    return results;
  };

  // Return all the elements for which a truth test fails.
  _.reject = function(obj, iterator, context) {
    return _.filter(obj, function(value, index, list) {
      return !iterator.call(context, value, index, list);
    }, context);
  };

  // Determine whether all of the elements match a truth test.
  // Delegates to **ECMAScript 5**'s native `every` if available.
  // Aliased as `all`.
  _.every = _.all = function(obj, iterator, context) {
    iterator || (iterator = _.identity);
    var result = true;
    if (obj == null) return result;
    if (nativeEvery && obj.every === nativeEvery) return obj.every(iterator, context);
    each(obj, function(value, index, list) {
      if (!(result = result && iterator.call(context, value, index, list))) return breaker;
    });
    return !!result;
  };

  // Determine if at least one element in the object matches a truth test.
  // Delegates to **ECMAScript 5**'s native `some` if available.
  // Aliased as `any`.
  var any = _.some = _.any = function(obj, iterator, context) {
    iterator || (iterator = _.identity);
    var result = false;
    if (obj == null) return result;
    if (nativeSome && obj.some === nativeSome) return obj.some(iterator, context);
    each(obj, function(value, index, list) {
      if (result || (result = iterator.call(context, value, index, list))) return breaker;
    });
    return !!result;
  };

  // Determine if the array or object contains a given value (using `===`).
  // Aliased as `include`.
  _.contains = _.include = function(obj, target) {
    if (obj == null) return false;
    if (nativeIndexOf && obj.indexOf === nativeIndexOf) return obj.indexOf(target) != -1;
    return any(obj, function(value) {
      return value === target;
    });
  };

  // Invoke a method (with arguments) on every item in a collection.
  _.invoke = function(obj, method) {
    var args = slice.call(arguments, 2);
    var isFunc = _.isFunction(method);
    return _.map(obj, function(value) {
      return (isFunc ? method : value[method]).apply(value, args);
    });
  };

  // Convenience version of a common use case of `map`: fetching a property.
  _.pluck = function(obj, key) {
    return _.map(obj, function(value){ return value[key]; });
  };

  // Convenience version of a common use case of `filter`: selecting only objects
  // containing specific `key:value` pairs.
  _.where = function(obj, attrs, first) {
    if (_.isEmpty(attrs)) return first ? void 0 : [];
    return _[first ? 'find' : 'filter'](obj, function(value) {
      for (var key in attrs) {
        if (attrs[key] !== value[key]) return false;
      }
      return true;
    });
  };

  // Convenience version of a common use case of `find`: getting the first object
  // containing specific `key:value` pairs.
  _.findWhere = function(obj, attrs) {
    return _.where(obj, attrs, true);
  };

  // Return the maximum element or (element-based computation).
  // Can't optimize arrays of integers longer than 65,535 elements.
  // See [WebKit Bug 80797](https://bugs.webkit.org/show_bug.cgi?id=80797)
  _.max = function(obj, iterator, context) {
    if (!iterator && _.isArray(obj) && obj[0] === +obj[0] && obj.length < 65535) {
      return Math.max.apply(Math, obj);
    }
    if (!iterator && _.isEmpty(obj)) return -Infinity;
    var result = {computed : -Infinity, value: -Infinity};
    each(obj, function(value, index, list) {
      var computed = iterator ? iterator.call(context, value, index, list) : value;
      computed > result.computed && (result = {value : value, computed : computed});
    });
    return result.value;
  };

  // Return the minimum element (or element-based computation).
  _.min = function(obj, iterator, context) {
    if (!iterator && _.isArray(obj) && obj[0] === +obj[0] && obj.length < 65535) {
      return Math.min.apply(Math, obj);
    }
    if (!iterator && _.isEmpty(obj)) return Infinity;
    var result = {computed : Infinity, value: Infinity};
    each(obj, function(value, index, list) {
      var computed = iterator ? iterator.call(context, value, index, list) : value;
      computed < result.computed && (result = {value : value, computed : computed});
    });
    return result.value;
  };

  // Shuffle an array, using the modern version of the 
  // [Fisher-Yates shuffle](http://en.wikipedia.org/wiki/Fisher–Yates_shuffle).
  _.shuffle = function(obj) {
    var rand;
    var index = 0;
    var shuffled = [];
    each(obj, function(value) {
      rand = _.random(index++);
      shuffled[index - 1] = shuffled[rand];
      shuffled[rand] = value;
    });
    return shuffled;
  };

  // Sample **n** random values from an array.
  // If **n** is not specified, returns a single random element from the array.
  // The internal `guard` argument allows it to work with `map`.
  _.sample = function(obj, n, guard) {
    if (arguments.length < 2 || guard) {
      return obj[_.random(obj.length - 1)];
    }
    return _.shuffle(obj).slice(0, Math.max(0, n));
  };

  // An internal function to generate lookup iterators.
  var lookupIterator = function(value) {
    return _.isFunction(value) ? value : function(obj){ return obj[value]; };
  };

  // Sort the object's values by a criterion produced by an iterator.
  _.sortBy = function(obj, value, context) {
    var iterator = lookupIterator(value);
    return _.pluck(_.map(obj, function(value, index, list) {
      return {
        value: value,
        index: index,
        criteria: iterator.call(context, value, index, list)
      };
    }).sort(function(left, right) {
      var a = left.criteria;
      var b = right.criteria;
      if (a !== b) {
        if (a > b || a === void 0) return 1;
        if (a < b || b === void 0) return -1;
      }
      return left.index - right.index;
    }), 'value');
  };

  // An internal function used for aggregate "group by" operations.
  var group = function(behavior) {
    return function(obj, value, context) {
      var result = {};
      var iterator = value == null ? _.identity : lookupIterator(value);
      each(obj, function(value, index) {
        var key = iterator.call(context, value, index, obj);
        behavior(result, key, value);
      });
      return result;
    };
  };

  // Groups the object's values by a criterion. Pass either a string attribute
  // to group by, or a function that returns the criterion.
  _.groupBy = group(function(result, key, value) {
    (_.has(result, key) ? result[key] : (result[key] = [])).push(value);
  });

  // Indexes the object's values by a criterion, similar to `groupBy`, but for
  // when you know that your index values will be unique.
  _.indexBy = group(function(result, key, value) {
    result[key] = value;
  });

  // Counts instances of an object that group by a certain criterion. Pass
  // either a string attribute to count by, or a function that returns the
  // criterion.
  _.countBy = group(function(result, key) {
    _.has(result, key) ? result[key]++ : result[key] = 1;
  });

  // Use a comparator function to figure out the smallest index at which
  // an object should be inserted so as to maintain order. Uses binary search.
  _.sortedIndex = function(array, obj, iterator, context) {
    iterator = iterator == null ? _.identity : lookupIterator(iterator);
    var value = iterator.call(context, obj);
    var low = 0, high = array.length;
    while (low < high) {
      var mid = (low + high) >>> 1;
      iterator.call(context, array[mid]) < value ? low = mid + 1 : high = mid;
    }
    return low;
  };

  // Safely create a real, live array from anything iterable.
  _.toArray = function(obj) {
    if (!obj) return [];
    if (_.isArray(obj)) return slice.call(obj);
    if (obj.length === +obj.length) return _.map(obj, _.identity);
    return _.values(obj);
  };

  // Return the number of elements in an object.
  _.size = function(obj) {
    if (obj == null) return 0;
    return (obj.length === +obj.length) ? obj.length : _.keys(obj).length;
  };

  // Array Functions
  // ---------------

  // Get the first element of an array. Passing **n** will return the first N
  // values in the array. Aliased as `head` and `take`. The **guard** check
  // allows it to work with `_.map`.
  _.first = _.head = _.take = function(array, n, guard) {
    if (array == null) return void 0;
    return (n == null) || guard ? array[0] : slice.call(array, 0, n);
  };

  // Returns everything but the last entry of the array. Especially useful on
  // the arguments object. Passing **n** will return all the values in
  // the array, excluding the last N. The **guard** check allows it to work with
  // `_.map`.
  _.initial = function(array, n, guard) {
    return slice.call(array, 0, array.length - ((n == null) || guard ? 1 : n));
  };

  // Get the last element of an array. Passing **n** will return the last N
  // values in the array. The **guard** check allows it to work with `_.map`.
  _.last = function(array, n, guard) {
    if (array == null) return void 0;
    if ((n == null) || guard) {
      return array[array.length - 1];
    } else {
      return slice.call(array, Math.max(array.length - n, 0));
    }
  };

  // Returns everything but the first entry of the array. Aliased as `tail` and `drop`.
  // Especially useful on the arguments object. Passing an **n** will return
  // the rest N values in the array. The **guard**
  // check allows it to work with `_.map`.
  _.rest = _.tail = _.drop = function(array, n, guard) {
    return slice.call(array, (n == null) || guard ? 1 : n);
  };

  // Trim out all falsy values from an array.
  _.compact = function(array) {
    return _.filter(array, _.identity);
  };

  // Internal implementation of a recursive `flatten` function.
  var flatten = function(input, shallow, output) {
    if (shallow && _.every(input, _.isArray)) {
      return concat.apply(output, input);
    }
    each(input, function(value) {
      if (_.isArray(value) || _.isArguments(value)) {
        shallow ? push.apply(output, value) : flatten(value, shallow, output);
      } else {
        output.push(value);
      }
    });
    return output;
  };

  // Flatten out an array, either recursively (by default), or just one level.
  _.flatten = function(array, shallow) {
    return flatten(array, shallow, []);
  };

  // Return a version of the array that does not contain the specified value(s).
  _.without = function(array) {
    return _.difference(array, slice.call(arguments, 1));
  };

  // Produce a duplicate-free version of the array. If the array has already
  // been sorted, you have the option of using a faster algorithm.
  // Aliased as `unique`.
  _.uniq = _.unique = function(array, isSorted, iterator, context) {
    if (_.isFunction(isSorted)) {
      context = iterator;
      iterator = isSorted;
      isSorted = false;
    }
    var initial = iterator ? _.map(array, iterator, context) : array;
    var results = [];
    var seen = [];
    each(initial, function(value, index) {
      if (isSorted ? (!index || seen[seen.length - 1] !== value) : !_.contains(seen, value)) {
        seen.push(value);
        results.push(array[index]);
      }
    });
    return results;
  };

  // Produce an array that contains the union: each distinct element from all of
  // the passed-in arrays.
  _.union = function() {
    return _.uniq(_.flatten(arguments, true));
  };

  // Produce an array that contains every item shared between all the
  // passed-in arrays.
  _.intersection = function(array) {
    var rest = slice.call(arguments, 1);
    return _.filter(_.uniq(array), function(item) {
      return _.every(rest, function(other) {
        return _.indexOf(other, item) >= 0;
      });
    });
  };

  // Take the difference between one array and a number of other arrays.
  // Only the elements present in just the first array will remain.
  _.difference = function(array) {
    var rest = concat.apply(ArrayProto, slice.call(arguments, 1));
    return _.filter(array, function(value){ return !_.contains(rest, value); });
  };

  // Zip together multiple lists into a single array -- elements that share
  // an index go together.
  _.zip = function() {
    var length = _.max(_.pluck(arguments, "length").concat(0));
    var results = new Array(length);
    for (var i = 0; i < length; i++) {
      results[i] = _.pluck(arguments, '' + i);
    }
    return results;
  };

  // Converts lists into objects. Pass either a single array of `[key, value]`
  // pairs, or two parallel arrays of the same length -- one of keys, and one of
  // the corresponding values.
  _.object = function(list, values) {
    if (list == null) return {};
    var result = {};
    for (var i = 0, length = list.length; i < length; i++) {
      if (values) {
        result[list[i]] = values[i];
      } else {
        result[list[i][0]] = list[i][1];
      }
    }
    return result;
  };

  // If the browser doesn't supply us with indexOf (I'm looking at you, **MSIE**),
  // we need this function. Return the position of the first occurrence of an
  // item in an array, or -1 if the item is not included in the array.
  // Delegates to **ECMAScript 5**'s native `indexOf` if available.
  // If the array is large and already in sort order, pass `true`
  // for **isSorted** to use binary search.
  _.indexOf = function(array, item, isSorted) {
    if (array == null) return -1;
    var i = 0, length = array.length;
    if (isSorted) {
      if (typeof isSorted == 'number') {
        i = (isSorted < 0 ? Math.max(0, length + isSorted) : isSorted);
      } else {
        i = _.sortedIndex(array, item);
        return array[i] === item ? i : -1;
      }
    }
    if (nativeIndexOf && array.indexOf === nativeIndexOf) return array.indexOf(item, isSorted);
    for (; i < length; i++) if (array[i] === item) return i;
    return -1;
  };

  // Delegates to **ECMAScript 5**'s native `lastIndexOf` if available.
  _.lastIndexOf = function(array, item, from) {
    if (array == null) return -1;
    var hasIndex = from != null;
    if (nativeLastIndexOf && array.lastIndexOf === nativeLastIndexOf) {
      return hasIndex ? array.lastIndexOf(item, from) : array.lastIndexOf(item);
    }
    var i = (hasIndex ? from : array.length);
    while (i--) if (array[i] === item) return i;
    return -1;
  };

  // Generate an integer Array containing an arithmetic progression. A port of
  // the native Python `range()` function. See
  // [the Python documentation](http://docs.python.org/library/functions.html#range).
  _.range = function(start, stop, step) {
    if (arguments.length <= 1) {
      stop = start || 0;
      start = 0;
    }
    step = arguments[2] || 1;

    var length = Math.max(Math.ceil((stop - start) / step), 0);
    var idx = 0;
    var range = new Array(length);

    while(idx < length) {
      range[idx++] = start;
      start += step;
    }

    return range;
  };

  // Function (ahem) Functions
  // ------------------

  // Reusable constructor function for prototype setting.
  var ctor = function(){};

  // Create a function bound to a given object (assigning `this`, and arguments,
  // optionally). Delegates to **ECMAScript 5**'s native `Function.bind` if
  // available.
  _.bind = function(func, context) {
    var args, bound;
    if (nativeBind && func.bind === nativeBind) return nativeBind.apply(func, slice.call(arguments, 1));
    if (!_.isFunction(func)) throw new TypeError;
    args = slice.call(arguments, 2);
    return bound = function() {
      if (!(this instanceof bound)) return func.apply(context, args.concat(slice.call(arguments)));
      ctor.prototype = func.prototype;
      var self = new ctor;
      ctor.prototype = null;
      var result = func.apply(self, args.concat(slice.call(arguments)));
      if (Object(result) === result) return result;
      return self;
    };
  };

  // Partially apply a function by creating a version that has had some of its
  // arguments pre-filled, without changing its dynamic `this` context.
  _.partial = function(func) {
    var args = slice.call(arguments, 1);
    return function() {
      return func.apply(this, args.concat(slice.call(arguments)));
    };
  };

  // Bind all of an object's methods to that object. Useful for ensuring that
  // all callbacks defined on an object belong to it.
  _.bindAll = function(obj) {
    var funcs = slice.call(arguments, 1);
    if (funcs.length === 0) throw new Error("bindAll must be passed function names");
    each(funcs, function(f) { obj[f] = _.bind(obj[f], obj); });
    return obj;
  };

  // Memoize an expensive function by storing its results.
  _.memoize = function(func, hasher) {
    var memo = {};
    hasher || (hasher = _.identity);
    return function() {
      var key = hasher.apply(this, arguments);
      return _.has(memo, key) ? memo[key] : (memo[key] = func.apply(this, arguments));
    };
  };

  // Delays a function for the given number of milliseconds, and then calls
  // it with the arguments supplied.
  _.delay = function(func, wait) {
    var args = slice.call(arguments, 2);
    return setTimeout(function(){ return func.apply(null, args); }, wait);
  };

  // Defers a function, scheduling it to run after the current call stack has
  // cleared.
  _.defer = function(func) {
    return _.delay.apply(_, [func, 1].concat(slice.call(arguments, 1)));
  };

  // Returns a function, that, when invoked, will only be triggered at most once
  // during a given window of time. Normally, the throttled function will run
  // as much as it can, without ever going more than once per `wait` duration;
  // but if you'd like to disable the execution on the leading edge, pass
  // `{leading: false}`. To disable execution on the trailing edge, ditto.
  _.throttle = function(func, wait, options) {
    var context, args, result;
    var timeout = null;
    var previous = 0;
    options || (options = {});
    var later = function() {
      previous = options.leading === false ? 0 : new Date;
      timeout = null;
      result = func.apply(context, args);
    };
    return function() {
      var now = new Date;
      if (!previous && options.leading === false) previous = now;
      var remaining = wait - (now - previous);
      context = this;
      args = arguments;
      if (remaining <= 0) {
        clearTimeout(timeout);
        timeout = null;
        previous = now;
        result = func.apply(context, args);
      } else if (!timeout && options.trailing !== false) {
        timeout = setTimeout(later, remaining);
      }
      return result;
    };
  };

  // Returns a function, that, as long as it continues to be invoked, will not
  // be triggered. The function will be called after it stops being called for
  // N milliseconds. If `immediate` is passed, trigger the function on the
  // leading edge, instead of the trailing.
  _.debounce = function(func, wait, immediate) {
    var timeout, args, context, timestamp, result;
    return function() {
      context = this;
      args = arguments;
      timestamp = new Date();
      var later = function() {
        var last = (new Date()) - timestamp;
        if (last < wait) {
          timeout = setTimeout(later, wait - last);
        } else {
          timeout = null;
          if (!immediate) result = func.apply(context, args);
        }
      };
      var callNow = immediate && !timeout;
      if (!timeout) {
        timeout = setTimeout(later, wait);
      }
      if (callNow) result = func.apply(context, args);
      return result;
    };
  };

  // Returns a function that will be executed at most one time, no matter how
  // often you call it. Useful for lazy initialization.
  _.once = function(func) {
    var ran = false, memo;
    return function() {
      if (ran) return memo;
      ran = true;
      memo = func.apply(this, arguments);
      func = null;
      return memo;
    };
  };

  // Returns the first function passed as an argument to the second,
  // allowing you to adjust arguments, run code before and after, and
  // conditionally execute the original function.
  _.wrap = function(func, wrapper) {
    return function() {
      var args = [func];
      push.apply(args, arguments);
      return wrapper.apply(this, args);
    };
  };

  // Returns a function that is the composition of a list of functions, each
  // consuming the return value of the function that follows.
  _.compose = function() {
    var funcs = arguments;
    return function() {
      var args = arguments;
      for (var i = funcs.length - 1; i >= 0; i--) {
        args = [funcs[i].apply(this, args)];
      }
      return args[0];
    };
  };

  // Returns a function that will only be executed after being called N times.
  _.after = function(times, func) {
    return function() {
      if (--times < 1) {
        return func.apply(this, arguments);
      }
    };
  };

  // Object Functions
  // ----------------

  // Retrieve the names of an object's properties.
  // Delegates to **ECMAScript 5**'s native `Object.keys`
  _.keys = nativeKeys || function(obj) {
    if (obj !== Object(obj)) throw new TypeError('Invalid object');
    var keys = [];
    for (var key in obj) if (_.has(obj, key)) keys.push(key);
    return keys;
  };

  // Retrieve the values of an object's properties.
  _.values = function(obj) {
    var keys = _.keys(obj);
    var length = keys.length;
    var values = new Array(length);
    for (var i = 0; i < length; i++) {
      values[i] = obj[keys[i]];
    }
    return values;
  };

  // Convert an object into a list of `[key, value]` pairs.
  _.pairs = function(obj) {
    var keys = _.keys(obj);
    var length = keys.length;
    var pairs = new Array(length);
    for (var i = 0; i < length; i++) {
      pairs[i] = [keys[i], obj[keys[i]]];
    }
    return pairs;
  };

  // Invert the keys and values of an object. The values must be serializable.
  _.invert = function(obj) {
    var result = {};
    var keys = _.keys(obj);
    for (var i = 0, length = keys.length; i < length; i++) {
      result[obj[keys[i]]] = keys[i];
    }
    return result;
  };

  // Return a sorted list of the function names available on the object.
  // Aliased as `methods`
  _.functions = _.methods = function(obj) {
    var names = [];
    for (var key in obj) {
      if (_.isFunction(obj[key])) names.push(key);
    }
    return names.sort();
  };

  // Extend a given object with all the properties in passed-in object(s).
  _.extend = function(obj) {
    each(slice.call(arguments, 1), function(source) {
      if (source) {
        for (var prop in source) {
          obj[prop] = source[prop];
        }
      }
    });
    return obj;
  };

  // Return a copy of the object only containing the whitelisted properties.
  _.pick = function(obj) {
    var copy = {};
    var keys = concat.apply(ArrayProto, slice.call(arguments, 1));
    each(keys, function(key) {
      if (key in obj) copy[key] = obj[key];
    });
    return copy;
  };

   // Return a copy of the object without the blacklisted properties.
  _.omit = function(obj) {
    var copy = {};
    var keys = concat.apply(ArrayProto, slice.call(arguments, 1));
    for (var key in obj) {
      if (!_.contains(keys, key)) copy[key] = obj[key];
    }
    return copy;
  };

  // Fill in a given object with default properties.
  _.defaults = function(obj) {
    each(slice.call(arguments, 1), function(source) {
      if (source) {
        for (var prop in source) {
          if (obj[prop] === void 0) obj[prop] = source[prop];
        }
      }
    });
    return obj;
  };

  // Create a (shallow-cloned) duplicate of an object.
  _.clone = function(obj) {
    if (!_.isObject(obj)) return obj;
    return _.isArray(obj) ? obj.slice() : _.extend({}, obj);
  };

  // Invokes interceptor with the obj, and then returns obj.
  // The primary purpose of this method is to "tap into" a method chain, in
  // order to perform operations on intermediate results within the chain.
  _.tap = function(obj, interceptor) {
    interceptor(obj);
    return obj;
  };

  // Internal recursive comparison function for `isEqual`.
  var eq = function(a, b, aStack, bStack) {
    // Identical objects are equal. `0 === -0`, but they aren't identical.
    // See the [Harmony `egal` proposal](http://wiki.ecmascript.org/doku.php?id=harmony:egal).
    if (a === b) return a !== 0 || 1 / a == 1 / b;
    // A strict comparison is necessary because `null == undefined`.
    if (a == null || b == null) return a === b;
    // Unwrap any wrapped objects.
    if (a instanceof _) a = a._wrapped;
    if (b instanceof _) b = b._wrapped;
    // Compare `[[Class]]` names.
    var className = toString.call(a);
    if (className != toString.call(b)) return false;
    switch (className) {
      // Strings, numbers, dates, and booleans are compared by value.
      case '[object String]':
        // Primitives and their corresponding object wrappers are equivalent; thus, `"5"` is
        // equivalent to `new String("5")`.
        return a == String(b);
      case '[object Number]':
        // `NaN`s are equivalent, but non-reflexive. An `egal` comparison is performed for
        // other numeric values.
        return a != +a ? b != +b : (a == 0 ? 1 / a == 1 / b : a == +b);
      case '[object Date]':
      case '[object Boolean]':
        // Coerce dates and booleans to numeric primitive values. Dates are compared by their
        // millisecond representations. Note that invalid dates with millisecond representations
        // of `NaN` are not equivalent.
        return +a == +b;
      // RegExps are compared by their source patterns and flags.
      case '[object RegExp]':
        return a.source == b.source &&
               a.global == b.global &&
               a.multiline == b.multiline &&
               a.ignoreCase == b.ignoreCase;
    }
    if (typeof a != 'object' || typeof b != 'object') return false;
    // Assume equality for cyclic structures. The algorithm for detecting cyclic
    // structures is adapted from ES 5.1 section 15.12.3, abstract operation `JO`.
    var length = aStack.length;
    while (length--) {
      // Linear search. Performance is inversely proportional to the number of
      // unique nested structures.
      if (aStack[length] == a) return bStack[length] == b;
    }
    // Objects with different constructors are not equivalent, but `Object`s
    // from different frames are.
    var aCtor = a.constructor, bCtor = b.constructor;
    if (aCtor !== bCtor && !(_.isFunction(aCtor) && (aCtor instanceof aCtor) &&
                             _.isFunction(bCtor) && (bCtor instanceof bCtor))) {
      return false;
    }
    // Add the first object to the stack of traversed objects.
    aStack.push(a);
    bStack.push(b);
    var size = 0, result = true;
    // Recursively compare objects and arrays.
    if (className == '[object Array]') {
      // Compare array lengths to determine if a deep comparison is necessary.
      size = a.length;
      result = size == b.length;
      if (result) {
        // Deep compare the contents, ignoring non-numeric properties.
        while (size--) {
          if (!(result = eq(a[size], b[size], aStack, bStack))) break;
        }
      }
    } else {
      // Deep compare objects.
      for (var key in a) {
        if (_.has(a, key)) {
          // Count the expected number of properties.
          size++;
          // Deep compare each member.
          if (!(result = _.has(b, key) && eq(a[key], b[key], aStack, bStack))) break;
        }
      }
      // Ensure that both objects contain the same number of properties.
      if (result) {
        for (key in b) {
          if (_.has(b, key) && !(size--)) break;
        }
        result = !size;
      }
    }
    // Remove the first object from the stack of traversed objects.
    aStack.pop();
    bStack.pop();
    return result;
  };

  // Perform a deep comparison to check if two objects are equal.
  _.isEqual = function(a, b) {
    return eq(a, b, [], []);
  };

  // Is a given array, string, or object empty?
  // An "empty" object has no enumerable own-properties.
  _.isEmpty = function(obj) {
    if (obj == null) return true;
    if (_.isArray(obj) || _.isString(obj)) return obj.length === 0;
    for (var key in obj) if (_.has(obj, key)) return false;
    return true;
  };

  // Is a given value a DOM element?
  _.isElement = function(obj) {
    return !!(obj && obj.nodeType === 1);
  };

  // Is a given value an array?
  // Delegates to ECMA5's native Array.isArray
  _.isArray = nativeIsArray || function(obj) {
    return toString.call(obj) == '[object Array]';
  };

  // Is a given variable an object?
  _.isObject = function(obj) {
    return obj === Object(obj);
  };

  // Add some isType methods: isArguments, isFunction, isString, isNumber, isDate, isRegExp.
  each(['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp'], function(name) {
    _['is' + name] = function(obj) {
      return toString.call(obj) == '[object ' + name + ']';
    };
  });

  // Define a fallback version of the method in browsers (ahem, IE), where
  // there isn't any inspectable "Arguments" type.
  if (!_.isArguments(arguments)) {
    _.isArguments = function(obj) {
      return !!(obj && _.has(obj, 'callee'));
    };
  }

  // Optimize `isFunction` if appropriate.
  if (typeof (/./) !== 'function') {
    _.isFunction = function(obj) {
      return typeof obj === 'function';
    };
  }

  // Is a given object a finite number?
  _.isFinite = function(obj) {
    return isFinite(obj) && !isNaN(parseFloat(obj));
  };

  // Is the given value `NaN`? (NaN is the only number which does not equal itself).
  _.isNaN = function(obj) {
    return _.isNumber(obj) && obj != +obj;
  };

  // Is a given value a boolean?
  _.isBoolean = function(obj) {
    return obj === true || obj === false || toString.call(obj) == '[object Boolean]';
  };

  // Is a given value equal to null?
  _.isNull = function(obj) {
    return obj === null;
  };

  // Is a given variable undefined?
  _.isUndefined = function(obj) {
    return obj === void 0;
  };

  // Shortcut function for checking if an object has a given property directly
  // on itself (in other words, not on a prototype).
  _.has = function(obj, key) {
    return hasOwnProperty.call(obj, key);
  };

  // Utility Functions
  // -----------------

  // Run Underscore.js in *noConflict* mode, returning the `_` variable to its
  // previous owner. Returns a reference to the Underscore object.
  _.noConflict = function() {
    root._ = previousUnderscore;
    return this;
  };

  // Keep the identity function around for default iterators.
  _.identity = function(value) {
    return value;
  };

  // Run a function **n** times.
  _.times = function(n, iterator, context) {
    var accum = Array(Math.max(0, n));
    for (var i = 0; i < n; i++) accum[i] = iterator.call(context, i);
    return accum;
  };

  // Return a random integer between min and max (inclusive).
  _.random = function(min, max) {
    if (max == null) {
      max = min;
      min = 0;
    }
    return min + Math.floor(Math.random() * (max - min + 1));
  };

  // List of HTML entities for escaping.
  var entityMap = {
    escape: {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#x27;'
    }
  };
  entityMap.unescape = _.invert(entityMap.escape);

  // Regexes containing the keys and values listed immediately above.
  var entityRegexes = {
    escape:   new RegExp('[' + _.keys(entityMap.escape).join('') + ']', 'g'),
    unescape: new RegExp('(' + _.keys(entityMap.unescape).join('|') + ')', 'g')
  };

  // Functions for escaping and unescaping strings to/from HTML interpolation.
  _.each(['escape', 'unescape'], function(method) {
    _[method] = function(string) {
      if (string == null) return '';
      return ('' + string).replace(entityRegexes[method], function(match) {
        return entityMap[method][match];
      });
    };
  });

  // If the value of the named `property` is a function then invoke it with the
  // `object` as context; otherwise, return it.
  _.result = function(object, property) {
    if (object == null) return void 0;
    var value = object[property];
    return _.isFunction(value) ? value.call(object) : value;
  };

  // Add your own custom functions to the Underscore object.
  _.mixin = function(obj) {
    each(_.functions(obj), function(name) {
      var func = _[name] = obj[name];
      _.prototype[name] = function() {
        var args = [this._wrapped];
        push.apply(args, arguments);
        return result.call(this, func.apply(_, args));
      };
    });
  };

  // Generate a unique integer id (unique within the entire client session).
  // Useful for temporary DOM ids.
  var idCounter = 0;
  _.uniqueId = function(prefix) {
    var id = ++idCounter + '';
    return prefix ? prefix + id : id;
  };

  // By default, Underscore uses ERB-style template delimiters, change the
  // following template settings to use alternative delimiters.
  _.templateSettings = {
    evaluate    : /<%([\s\S]+?)%>/g,
    interpolate : /<%=([\s\S]+?)%>/g,
    escape      : /<%-([\s\S]+?)%>/g
  };

  // When customizing `templateSettings`, if you don't want to define an
  // interpolation, evaluation or escaping regex, we need one that is
  // guaranteed not to match.
  var noMatch = /(.)^/;

  // Certain characters need to be escaped so that they can be put into a
  // string literal.
  var escapes = {
    "'":      "'",
    '\\':     '\\',
    '\r':     'r',
    '\n':     'n',
    '\t':     't',
    '\u2028': 'u2028',
    '\u2029': 'u2029'
  };

  var escaper = /\\|'|\r|\n|\t|\u2028|\u2029/g;

  // JavaScript micro-templating, similar to John Resig's implementation.
  // Underscore templating handles arbitrary delimiters, preserves whitespace,
  // and correctly escapes quotes within interpolated code.
  _.template = function(text, data, settings) {
    var render;
    settings = _.defaults({}, settings, _.templateSettings);

    // Combine delimiters into one regular expression via alternation.
    var matcher = new RegExp([
      (settings.escape || noMatch).source,
      (settings.interpolate || noMatch).source,
      (settings.evaluate || noMatch).source
    ].join('|') + '|$', 'g');

    // Compile the template source, escaping string literals appropriately.
    var index = 0;
    var source = "__p+='";
    text.replace(matcher, function(match, escape, interpolate, evaluate, offset) {
      source += text.slice(index, offset)
        .replace(escaper, function(match) { return '\\' + escapes[match]; });

      if (escape) {
        source += "'+\n((__t=(" + escape + "))==null?'':_.escape(__t))+\n'";
      }
      if (interpolate) {
        source += "'+\n((__t=(" + interpolate + "))==null?'':__t)+\n'";
      }
      if (evaluate) {
        source += "';\n" + evaluate + "\n__p+='";
      }
      index = offset + match.length;
      return match;
    });
    source += "';\n";

    // If a variable is not specified, place data values in local scope.
    if (!settings.variable) source = 'with(obj||{}){\n' + source + '}\n';

    source = "var __t,__p='',__j=Array.prototype.join," +
      "print=function(){__p+=__j.call(arguments,'');};\n" +
      source + "return __p;\n";

    try {
      render = new Function(settings.variable || 'obj', '_', source);
    } catch (e) {
      e.source = source;
      throw e;
    }

    if (data) return render(data, _);
    var template = function(data) {
      return render.call(this, data, _);
    };

    // Provide the compiled function source as a convenience for precompilation.
    template.source = 'function(' + (settings.variable || 'obj') + '){\n' + source + '}';

    return template;
  };

  // Add a "chain" function, which will delegate to the wrapper.
  _.chain = function(obj) {
    return _(obj).chain();
  };

  // OOP
  // ---------------
  // If Underscore is called as a function, it returns a wrapped object that
  // can be used OO-style. This wrapper holds altered versions of all the
  // underscore functions. Wrapped objects may be chained.

  // Helper function to continue chaining intermediate results.
  var result = function(obj) {
    return this._chain ? _(obj).chain() : obj;
  };

  // Add all of the Underscore functions to the wrapper object.
  _.mixin(_);

  // Add all mutator Array functions to the wrapper.
  each(['pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'], function(name) {
    var method = ArrayProto[name];
    _.prototype[name] = function() {
      var obj = this._wrapped;
      method.apply(obj, arguments);
      if ((name == 'shift' || name == 'splice') && obj.length === 0) delete obj[0];
      return result.call(this, obj);
    };
  });

  // Add all accessor Array functions to the wrapper.
  each(['concat', 'join', 'slice'], function(name) {
    var method = ArrayProto[name];
    _.prototype[name] = function() {
      return result.call(this, method.apply(this._wrapped, arguments));
    };
  });

  _.extend(_.prototype, {

    // Start chaining a wrapped Underscore object.
    chain: function() {
      this._chain = true;
      return this;
    },

    // Extracts the result from a wrapped and chained object.
    value: function() {
      return this._wrapped;
    }

  });

}).call(this);

/**
 * @license AngularJS v1.2.30
 * (c) 2010-2014 Google, Inc. http://angularjs.org
 * License: MIT
 */
(function(window, angular, undefined) {'use strict';

/**
 * @ngdoc module
 * @name ngCookies
 * @description
 *
 * # ngCookies
 *
 * The `ngCookies` module provides a convenient wrapper for reading and writing browser cookies.
 *
 *
 * <div doc-module-components="ngCookies"></div>
 *
 * See {@link ngCookies.$cookies `$cookies`} and
 * {@link ngCookies.$cookieStore `$cookieStore`} for usage.
 */


angular.module('ngCookies', ['ng']).
  /**
   * @ngdoc service
   * @name $cookies
   *
   * @description
   * Provides read/write access to browser's cookies.
   *
   * Only a simple Object is exposed and by adding or removing properties to/from this object, new
   * cookies are created/deleted at the end of current $eval.
   * The object's properties can only be strings.
   *
   * Requires the {@link ngCookies `ngCookies`} module to be installed.
   *
   * @example
   *
   * ```js
   * angular.module('cookiesExample', ['ngCookies'])
   *   .controller('ExampleController', ['$cookies', function($cookies) {
   *     // Retrieving a cookie
   *     var favoriteCookie = $cookies.myFavorite;
   *     // Setting a cookie
   *     $cookies.myFavorite = 'oatmeal';
   *   }]);
   * ```
   */
   factory('$cookies', ['$rootScope', '$browser', function ($rootScope, $browser) {
      var cookies = {},
          lastCookies = {},
          lastBrowserCookies,
          runEval = false,
          copy = angular.copy,
          isUndefined = angular.isUndefined;

      //creates a poller fn that copies all cookies from the $browser to service & inits the service
      $browser.addPollFn(function() {
        var currentCookies = $browser.cookies();
        if (lastBrowserCookies != currentCookies) { //relies on browser.cookies() impl
          lastBrowserCookies = currentCookies;
          copy(currentCookies, lastCookies);
          copy(currentCookies, cookies);
          if (runEval) $rootScope.$apply();
        }
      })();

      runEval = true;

      //at the end of each eval, push cookies
      //TODO: this should happen before the "delayed" watches fire, because if some cookies are not
      //      strings or browser refuses to store some cookies, we update the model in the push fn.
      $rootScope.$watch(push);

      return cookies;


      /**
       * Pushes all the cookies from the service to the browser and verifies if all cookies were
       * stored.
       */
      function push() {
        var name,
            value,
            browserCookies,
            updated;

        //delete any cookies deleted in $cookies
        for (name in lastCookies) {
          if (isUndefined(cookies[name])) {
            $browser.cookies(name, undefined);
          }
        }

        //update all cookies updated in $cookies
        for(name in cookies) {
          value = cookies[name];
          if (!angular.isString(value)) {
            value = '' + value;
            cookies[name] = value;
          }
          if (value !== lastCookies[name]) {
            $browser.cookies(name, value);
            updated = true;
          }
        }

        //verify what was actually stored
        if (updated){
          updated = false;
          browserCookies = $browser.cookies();

          for (name in cookies) {
            if (cookies[name] !== browserCookies[name]) {
              //delete or reset all cookies that the browser dropped from $cookies
              if (isUndefined(browserCookies[name])) {
                delete cookies[name];
              } else {
                cookies[name] = browserCookies[name];
              }
              updated = true;
            }
          }
        }
      }
    }]).


  /**
   * @ngdoc service
   * @name $cookieStore
   * @requires $cookies
   *
   * @description
   * Provides a key-value (string-object) storage, that is backed by session cookies.
   * Objects put or retrieved from this storage are automatically serialized or
   * deserialized by angular's toJson/fromJson.
   *
   * Requires the {@link ngCookies `ngCookies`} module to be installed.
   *
   * @example
   *
   * ```js
   * angular.module('cookieStoreExample', ['ngCookies'])
   *   .controller('ExampleController', ['$cookieStore', function($cookieStore) {
   *     // Put cookie
   *     $cookieStore.put('myFavorite','oatmeal');
   *     // Get cookie
   *     var favoriteCookie = $cookieStore.get('myFavorite');
   *     // Removing a cookie
   *     $cookieStore.remove('myFavorite');
   *   }]);
   * ```
   */
   factory('$cookieStore', ['$cookies', function($cookies) {

      return {
        /**
         * @ngdoc method
         * @name $cookieStore#get
         *
         * @description
         * Returns the value of given cookie key
         *
         * @param {string} key Id to use for lookup.
         * @returns {Object} Deserialized cookie value.
         */
        get: function(key) {
          var value = $cookies[key];
          return value ? angular.fromJson(value) : value;
        },

        /**
         * @ngdoc method
         * @name $cookieStore#put
         *
         * @description
         * Sets a value for given cookie key
         *
         * @param {string} key Id for the `value`.
         * @param {Object} value Value to be stored.
         */
        put: function(key, value) {
          $cookies[key] = angular.toJson(value);
        },

        /**
         * @ngdoc method
         * @name $cookieStore#remove
         *
         * @description
         * Remove given cookie
         *
         * @param {string} key Id of the key-value pair to delete.
         */
        remove: function(key) {
          delete $cookies[key];
        }
      };

    }]);


})(window, window.angular);

/**
 * @license AngularJS v1.2.30
 * (c) 2010-2014 Google, Inc. http://angularjs.org
 * License: MIT
 */
(function(window, angular, undefined) {'use strict';

var $sanitizeMinErr = angular.$$minErr('$sanitize');

/**
 * @ngdoc module
 * @name ngSanitize
 * @description
 *
 * # ngSanitize
 *
 * The `ngSanitize` module provides functionality to sanitize HTML.
 *
 *
 * <div doc-module-components="ngSanitize"></div>
 *
 * See {@link ngSanitize.$sanitize `$sanitize`} for usage.
 */

/*
 * HTML Parser By Misko Hevery (misko@hevery.com)
 * based on:  HTML Parser By John Resig (ejohn.org)
 * Original code by Erik Arvidsson, Mozilla Public License
 * http://erik.eae.net/simplehtmlparser/simplehtmlparser.js
 *
 * // Use like so:
 * htmlParser(htmlString, {
 *     start: function(tag, attrs, unary) {},
 *     end: function(tag) {},
 *     chars: function(text) {},
 *     comment: function(text) {}
 * });
 *
 */


/**
 * @ngdoc service
 * @name $sanitize
 * @kind function
 *
 * @description
 *   The input is sanitized by parsing the html into tokens. All safe tokens (from a whitelist) are
 *   then serialized back to properly escaped html string. This means that no unsafe input can make
 *   it into the returned string, however, since our parser is more strict than a typical browser
 *   parser, it's possible that some obscure input, which would be recognized as valid HTML by a
 *   browser, won't make it through the sanitizer.
 *   The whitelist is configured using the functions `aHrefSanitizationWhitelist` and
 *   `imgSrcSanitizationWhitelist` of {@link ng.$compileProvider `$compileProvider`}.
 *
 * @param {string} html Html input.
 * @returns {string} Sanitized html.
 *
 * @example
   <example module="sanitizeExample" deps="angular-sanitize.js">
   <file name="index.html">
     <script>
         angular.module('sanitizeExample', ['ngSanitize'])
           .controller('ExampleController', ['$scope', '$sce', function($scope, $sce) {
             $scope.snippet =
               '<p style="color:blue">an html\n' +
               '<em onmouseover="this.textContent=\'PWN3D!\'">click here</em>\n' +
               'snippet</p>';
             $scope.deliberatelyTrustDangerousSnippet = function() {
               return $sce.trustAsHtml($scope.snippet);
             };
           }]);
     </script>
     <div ng-controller="ExampleController">
        Snippet: <textarea ng-model="snippet" cols="60" rows="3"></textarea>
       <table>
         <tr>
           <td>Directive</td>
           <td>How</td>
           <td>Source</td>
           <td>Rendered</td>
         </tr>
         <tr id="bind-html-with-sanitize">
           <td>ng-bind-html</td>
           <td>Automatically uses $sanitize</td>
           <td><pre>&lt;div ng-bind-html="snippet"&gt;<br/>&lt;/div&gt;</pre></td>
           <td><div ng-bind-html="snippet"></div></td>
         </tr>
         <tr id="bind-html-with-trust">
           <td>ng-bind-html</td>
           <td>Bypass $sanitize by explicitly trusting the dangerous value</td>
           <td>
           <pre>&lt;div ng-bind-html="deliberatelyTrustDangerousSnippet()"&gt;
&lt;/div&gt;</pre>
           </td>
           <td><div ng-bind-html="deliberatelyTrustDangerousSnippet()"></div></td>
         </tr>
         <tr id="bind-default">
           <td>ng-bind</td>
           <td>Automatically escapes</td>
           <td><pre>&lt;div ng-bind="snippet"&gt;<br/>&lt;/div&gt;</pre></td>
           <td><div ng-bind="snippet"></div></td>
         </tr>
       </table>
       </div>
   </file>
   <file name="protractor.js" type="protractor">
     it('should sanitize the html snippet by default', function() {
       expect(element(by.css('#bind-html-with-sanitize div')).getInnerHtml()).
         toBe('<p>an html\n<em>click here</em>\nsnippet</p>');
     });

     it('should inline raw snippet if bound to a trusted value', function() {
       expect(element(by.css('#bind-html-with-trust div')).getInnerHtml()).
         toBe("<p style=\"color:blue\">an html\n" +
              "<em onmouseover=\"this.textContent='PWN3D!'\">click here</em>\n" +
              "snippet</p>");
     });

     it('should escape snippet without any filter', function() {
       expect(element(by.css('#bind-default div')).getInnerHtml()).
         toBe("&lt;p style=\"color:blue\"&gt;an html\n" +
              "&lt;em onmouseover=\"this.textContent='PWN3D!'\"&gt;click here&lt;/em&gt;\n" +
              "snippet&lt;/p&gt;");
     });

     it('should update', function() {
       element(by.model('snippet')).clear();
       element(by.model('snippet')).sendKeys('new <b onclick="alert(1)">text</b>');
       expect(element(by.css('#bind-html-with-sanitize div')).getInnerHtml()).
         toBe('new <b>text</b>');
       expect(element(by.css('#bind-html-with-trust div')).getInnerHtml()).toBe(
         'new <b onclick="alert(1)">text</b>');
       expect(element(by.css('#bind-default div')).getInnerHtml()).toBe(
         "new &lt;b onclick=\"alert(1)\"&gt;text&lt;/b&gt;");
     });
   </file>
   </example>
 */
function $SanitizeProvider() {
  this.$get = ['$$sanitizeUri', function($$sanitizeUri) {
    return function(html) {
      var buf = [];
      htmlParser(html, htmlSanitizeWriter(buf, function(uri, isImage) {
        return !/^unsafe/.test($$sanitizeUri(uri, isImage));
      }));
      return buf.join('');
    };
  }];
}

function sanitizeText(chars) {
  var buf = [];
  var writer = htmlSanitizeWriter(buf, angular.noop);
  writer.chars(chars);
  return buf.join('');
}


// Regular Expressions for parsing tags and attributes
var START_TAG_REGEXP =
       /^<((?:[a-zA-Z])[\w:-]*)((?:\s+[\w:-]+(?:\s*=\s*(?:(?:"[^"]*")|(?:'[^']*')|[^>\s]+))?)*)\s*(\/?)\s*(>?)/,
  END_TAG_REGEXP = /^<\/\s*([\w:-]+)[^>]*>/,
  ATTR_REGEXP = /([\w:-]+)(?:\s*=\s*(?:(?:"((?:[^"])*)")|(?:'((?:[^'])*)')|([^>\s]+)))?/g,
  BEGIN_TAG_REGEXP = /^</,
  BEGING_END_TAGE_REGEXP = /^<\//,
  COMMENT_REGEXP = /<!--(.*?)-->/g,
  DOCTYPE_REGEXP = /<!DOCTYPE([^>]*?)>/i,
  CDATA_REGEXP = /<!\[CDATA\[(.*?)]]>/g,
  SURROGATE_PAIR_REGEXP = /[\uD800-\uDBFF][\uDC00-\uDFFF]/g,
  // Match everything outside of normal chars and " (quote character)
  NON_ALPHANUMERIC_REGEXP = /([^\#-~| |!])/g;


// Good source of info about elements and attributes
// http://dev.w3.org/html5/spec/Overview.html#semantics
// http://simon.html5.org/html-elements

// Safe Void Elements - HTML5
// http://dev.w3.org/html5/spec/Overview.html#void-elements
var voidElements = makeMap("area,br,col,hr,img,wbr");

// Elements that you can, intentionally, leave open (and which close themselves)
// http://dev.w3.org/html5/spec/Overview.html#optional-tags
var optionalEndTagBlockElements = makeMap("colgroup,dd,dt,li,p,tbody,td,tfoot,th,thead,tr"),
    optionalEndTagInlineElements = makeMap("rp,rt"),
    optionalEndTagElements = angular.extend({},
                                            optionalEndTagInlineElements,
                                            optionalEndTagBlockElements);

// Safe Block Elements - HTML5
var blockElements = angular.extend({}, optionalEndTagBlockElements, makeMap("address,article," +
        "aside,blockquote,caption,center,del,dir,div,dl,figure,figcaption,footer,h1,h2,h3,h4,h5," +
        "h6,header,hgroup,hr,ins,map,menu,nav,ol,pre,script,section,table,ul"));

// Inline Elements - HTML5
var inlineElements = angular.extend({}, optionalEndTagInlineElements, makeMap("a,abbr,acronym,b," +
        "bdi,bdo,big,br,cite,code,del,dfn,em,font,i,img,ins,kbd,label,map,mark,q,ruby,rp,rt,s," +
        "samp,small,span,strike,strong,sub,sup,time,tt,u,var"));


// Special Elements (can contain anything)
var specialElements = makeMap("script,style");

var validElements = angular.extend({},
                                   voidElements,
                                   blockElements,
                                   inlineElements,
                                   optionalEndTagElements);

//Attributes that have href and hence need to be sanitized
var uriAttrs = makeMap("background,cite,href,longdesc,src");
var validAttrs = angular.extend({}, uriAttrs, makeMap(
    'abbr,align,alt,axis,bgcolor,border,cellpadding,cellspacing,class,clear,'+
    'color,cols,colspan,compact,coords,dir,face,headers,height,hreflang,hspace,'+
    'ismap,lang,language,nohref,nowrap,rel,rev,rows,rowspan,rules,'+
    'scope,scrolling,shape,size,span,start,summary,target,title,type,'+
    'valign,value,vspace,width'));

function makeMap(str) {
  var obj = {}, items = str.split(','), i;
  for (i = 0; i < items.length; i++) obj[items[i]] = true;
  return obj;
}


/**
 * @example
 * htmlParser(htmlString, {
 *     start: function(tag, attrs, unary) {},
 *     end: function(tag) {},
 *     chars: function(text) {},
 *     comment: function(text) {}
 * });
 *
 * @param {string} html string
 * @param {object} handler
 */
function htmlParser( html, handler ) {
  if (typeof html !== 'string') {
    if (html === null || typeof html === 'undefined') {
      html = '';
    } else {
      html = '' + html;
    }
  }
  var index, chars, match, stack = [], last = html, text;
  stack.last = function() { return stack[ stack.length - 1 ]; };

  while ( html ) {
    text = '';
    chars = true;

    // Make sure we're not in a script or style element
    if ( !stack.last() || !specialElements[ stack.last() ] ) {

      // Comment
      if ( html.indexOf("<!--") === 0 ) {
        // comments containing -- are not allowed unless they terminate the comment
        index = html.indexOf("--", 4);

        if ( index >= 0 && html.lastIndexOf("-->", index) === index) {
          if (handler.comment) handler.comment( html.substring( 4, index ) );
          html = html.substring( index + 3 );
          chars = false;
        }
      // DOCTYPE
      } else if ( DOCTYPE_REGEXP.test(html) ) {
        match = html.match( DOCTYPE_REGEXP );

        if ( match ) {
          html = html.replace( match[0], '');
          chars = false;
        }
      // end tag
      } else if ( BEGING_END_TAGE_REGEXP.test(html) ) {
        match = html.match( END_TAG_REGEXP );

        if ( match ) {
          html = html.substring( match[0].length );
          match[0].replace( END_TAG_REGEXP, parseEndTag );
          chars = false;
        }

      // start tag
      } else if ( BEGIN_TAG_REGEXP.test(html) ) {
        match = html.match( START_TAG_REGEXP );

        if ( match ) {
          // We only have a valid start-tag if there is a '>'.
          if ( match[4] ) {
            html = html.substring( match[0].length );
            match[0].replace( START_TAG_REGEXP, parseStartTag );
          }
          chars = false;
        } else {
          // no ending tag found --- this piece should be encoded as an entity.
          text += '<';
          html = html.substring(1);
        }
      }

      if ( chars ) {
        index = html.indexOf("<");

        text += index < 0 ? html : html.substring( 0, index );
        html = index < 0 ? "" : html.substring( index );

        if (handler.chars) handler.chars( decodeEntities(text) );
      }

    } else {
      html = html.replace(new RegExp("(.*)<\\s*\\/\\s*" + stack.last() + "[^>]*>", 'i'),
        function(all, text){
          text = text.replace(COMMENT_REGEXP, "$1").replace(CDATA_REGEXP, "$1");

          if (handler.chars) handler.chars( decodeEntities(text) );

          return "";
      });

      parseEndTag( "", stack.last() );
    }

    if ( html == last ) {
      throw $sanitizeMinErr('badparse', "The sanitizer was unable to parse the following block " +
                                        "of html: {0}", html);
    }
    last = html;
  }

  // Clean up any remaining tags
  parseEndTag();

  function parseStartTag( tag, tagName, rest, unary ) {
    tagName = angular.lowercase(tagName);
    if ( blockElements[ tagName ] ) {
      while ( stack.last() && inlineElements[ stack.last() ] ) {
        parseEndTag( "", stack.last() );
      }
    }

    if ( optionalEndTagElements[ tagName ] && stack.last() == tagName ) {
      parseEndTag( "", tagName );
    }

    unary = voidElements[ tagName ] || !!unary;

    if ( !unary )
      stack.push( tagName );

    var attrs = {};

    rest.replace(ATTR_REGEXP,
      function(match, name, doubleQuotedValue, singleQuotedValue, unquotedValue) {
        var value = doubleQuotedValue
          || singleQuotedValue
          || unquotedValue
          || '';

        attrs[name] = decodeEntities(value);
    });
    if (handler.start) handler.start( tagName, attrs, unary );
  }

  function parseEndTag( tag, tagName ) {
    var pos = 0, i;
    tagName = angular.lowercase(tagName);
    if ( tagName )
      // Find the closest opened tag of the same type
      for ( pos = stack.length - 1; pos >= 0; pos-- )
        if ( stack[ pos ] == tagName )
          break;

    if ( pos >= 0 ) {
      // Close all the open elements, up the stack
      for ( i = stack.length - 1; i >= pos; i-- )
        if (handler.end) handler.end( stack[ i ] );

      // Remove the open elements from the stack
      stack.length = pos;
    }
  }
}

var hiddenPre=document.createElement("pre");
var spaceRe = /^(\s*)([\s\S]*?)(\s*)$/;
/**
 * decodes all entities into regular string
 * @param value
 * @returns {string} A string with decoded entities.
 */
function decodeEntities(value) {
  if (!value) { return ''; }

  // Note: IE8 does not preserve spaces at the start/end of innerHTML
  // so we must capture them and reattach them afterward
  var parts = spaceRe.exec(value);
  var spaceBefore = parts[1];
  var spaceAfter = parts[3];
  var content = parts[2];
  if (content) {
    hiddenPre.innerHTML=content.replace(/</g,"&lt;");
    // innerText depends on styling as it doesn't display hidden elements.
    // Therefore, it's better to use textContent not to cause unnecessary
    // reflows. However, IE<9 don't support textContent so the innerText
    // fallback is necessary.
    content = 'textContent' in hiddenPre ?
      hiddenPre.textContent : hiddenPre.innerText;
  }
  return spaceBefore + content + spaceAfter;
}

/**
 * Escapes all potentially dangerous characters, so that the
 * resulting string can be safely inserted into attribute or
 * element text.
 * @param value
 * @returns {string} escaped text
 */
function encodeEntities(value) {
  return value.
    replace(/&/g, '&amp;').
    replace(SURROGATE_PAIR_REGEXP, function (value) {
      var hi = value.charCodeAt(0);
      var low = value.charCodeAt(1);
      return '&#' + (((hi - 0xD800) * 0x400) + (low - 0xDC00) + 0x10000) + ';';
    }).
    replace(NON_ALPHANUMERIC_REGEXP, function(value){
      return '&#' + value.charCodeAt(0) + ';';
    }).
    replace(/</g, '&lt;').
    replace(/>/g, '&gt;');
}

/**
 * create an HTML/XML writer which writes to buffer
 * @param {Array} buf use buf.jain('') to get out sanitized html string
 * @returns {object} in the form of {
 *     start: function(tag, attrs, unary) {},
 *     end: function(tag) {},
 *     chars: function(text) {},
 *     comment: function(text) {}
 * }
 */
function htmlSanitizeWriter(buf, uriValidator){
  var ignore = false;
  var out = angular.bind(buf, buf.push);
  return {
    start: function(tag, attrs, unary){
      tag = angular.lowercase(tag);
      if (!ignore && specialElements[tag]) {
        ignore = tag;
      }
      if (!ignore && validElements[tag] === true) {
        out('<');
        out(tag);
        angular.forEach(attrs, function(value, key){
          var lkey=angular.lowercase(key);
          var isImage = (tag === 'img' && lkey === 'src') || (lkey === 'background');
          if (validAttrs[lkey] === true &&
            (uriAttrs[lkey] !== true || uriValidator(value, isImage))) {
            out(' ');
            out(key);
            out('="');
            out(encodeEntities(value));
            out('"');
          }
        });
        out(unary ? '/>' : '>');
      }
    },
    end: function(tag){
        tag = angular.lowercase(tag);
        if (!ignore && validElements[tag] === true) {
          out('</');
          out(tag);
          out('>');
        }
        if (tag == ignore) {
          ignore = false;
        }
      },
    chars: function(chars){
        if (!ignore) {
          out(encodeEntities(chars));
        }
      }
  };
}


// define ngSanitize module and register $sanitize service
angular.module('ngSanitize', []).provider('$sanitize', $SanitizeProvider);

/* global sanitizeText: false */

/**
 * @ngdoc filter
 * @name linky
 * @kind function
 *
 * @description
 * Finds links in text input and turns them into html links. Supports http/https/ftp/mailto and
 * plain email address links.
 *
 * Requires the {@link ngSanitize `ngSanitize`} module to be installed.
 *
 * @param {string} text Input text.
 * @param {string} target Window (_blank|_self|_parent|_top) or named frame to open links in.
 * @returns {string} Html-linkified text.
 *
 * @usage
   <span ng-bind-html="linky_expression | linky"></span>
 *
 * @example
   <example module="linkyExample" deps="angular-sanitize.js">
     <file name="index.html">
       <script>
         angular.module('linkyExample', ['ngSanitize'])
           .controller('ExampleController', ['$scope', function($scope) {
             $scope.snippet =
               'Pretty text with some links:\n'+
               'http://angularjs.org/,\n'+
               'mailto:us@somewhere.org,\n'+
               'another@somewhere.org,\n'+
               'and one more: ftp://127.0.0.1/.';
             $scope.snippetWithTarget = 'http://angularjs.org/';
           }]);
       </script>
       <div ng-controller="ExampleController">
       Snippet: <textarea ng-model="snippet" cols="60" rows="3"></textarea>
       <table>
         <tr>
           <td>Filter</td>
           <td>Source</td>
           <td>Rendered</td>
         </tr>
         <tr id="linky-filter">
           <td>linky filter</td>
           <td>
             <pre>&lt;div ng-bind-html="snippet | linky"&gt;<br>&lt;/div&gt;</pre>
           </td>
           <td>
             <div ng-bind-html="snippet | linky"></div>
           </td>
         </tr>
         <tr id="linky-target">
          <td>linky target</td>
          <td>
            <pre>&lt;div ng-bind-html="snippetWithTarget | linky:'_blank'"&gt;<br>&lt;/div&gt;</pre>
          </td>
          <td>
            <div ng-bind-html="snippetWithTarget | linky:'_blank'"></div>
          </td>
         </tr>
         <tr id="escaped-html">
           <td>no filter</td>
           <td><pre>&lt;div ng-bind="snippet"&gt;<br>&lt;/div&gt;</pre></td>
           <td><div ng-bind="snippet"></div></td>
         </tr>
       </table>
     </file>
     <file name="protractor.js" type="protractor">
       it('should linkify the snippet with urls', function() {
         expect(element(by.id('linky-filter')).element(by.binding('snippet | linky')).getText()).
             toBe('Pretty text with some links: http://angularjs.org/, us@somewhere.org, ' +
                  'another@somewhere.org, and one more: ftp://127.0.0.1/.');
         expect(element.all(by.css('#linky-filter a')).count()).toEqual(4);
       });

       it('should not linkify snippet without the linky filter', function() {
         expect(element(by.id('escaped-html')).element(by.binding('snippet')).getText()).
             toBe('Pretty text with some links: http://angularjs.org/, mailto:us@somewhere.org, ' +
                  'another@somewhere.org, and one more: ftp://127.0.0.1/.');
         expect(element.all(by.css('#escaped-html a')).count()).toEqual(0);
       });

       it('should update', function() {
         element(by.model('snippet')).clear();
         element(by.model('snippet')).sendKeys('new http://link.');
         expect(element(by.id('linky-filter')).element(by.binding('snippet | linky')).getText()).
             toBe('new http://link.');
         expect(element.all(by.css('#linky-filter a')).count()).toEqual(1);
         expect(element(by.id('escaped-html')).element(by.binding('snippet')).getText())
             .toBe('new http://link.');
       });

       it('should work with the target property', function() {
        expect(element(by.id('linky-target')).
            element(by.binding("snippetWithTarget | linky:'_blank'")).getText()).
            toBe('http://angularjs.org/');
        expect(element(by.css('#linky-target a')).getAttribute('target')).toEqual('_blank');
       });
     </file>
   </example>
 */
angular.module('ngSanitize').filter('linky', ['$sanitize', function($sanitize) {
  var LINKY_URL_REGEXP =
        /((ftp|https?):\/\/|(mailto:)?[A-Za-z0-9._%+-]+@)\S*[^\s.;,(){}<>"]/,
      MAILTO_REGEXP = /^mailto:/;

  return function(text, target) {
    if (!text) return text;
    var match;
    var raw = text;
    var html = [];
    var url;
    var i;
    while ((match = raw.match(LINKY_URL_REGEXP))) {
      // We can not end in these as they are sometimes found at the end of the sentence
      url = match[0];
      // if we did not match ftp/http/mailto then assume mailto
      if (match[2] == match[3]) url = 'mailto:' + url;
      i = match.index;
      addText(raw.substr(0, i));
      addLink(url, match[0].replace(MAILTO_REGEXP, ''));
      raw = raw.substring(i + match[0].length);
    }
    addText(raw);
    return $sanitize(html.join(''));

    function addText(text) {
      if (!text) {
        return;
      }
      html.push(sanitizeText(text));
    }

    function addLink(url, text) {
      html.push('<a ');
      if (angular.isDefined(target)) {
        html.push('target="');
        html.push(target);
        html.push('" ');
      }
      html.push('href="',
                url.replace('"', '&quot;'),
                '">');
      addText(text);
      html.push('</a>');
    }
  };
}]);


})(window, window.angular);

angular.module("ui.bootstrap", ["ui.bootstrap.tpls", "ui.bootstrap.transition","ui.bootstrap.collapse","ui.bootstrap.accordion","ui.bootstrap.alert","ui.bootstrap.bindHtml","ui.bootstrap.buttons","ui.bootstrap.carousel","ui.bootstrap.position","ui.bootstrap.datepicker","ui.bootstrap.dropdownToggle","ui.bootstrap.modal","ui.bootstrap.pagination","ui.bootstrap.tooltip","ui.bootstrap.popover","ui.bootstrap.progressbar","ui.bootstrap.rating","ui.bootstrap.tabs","ui.bootstrap.timepicker","ui.bootstrap.typeahead"]);
angular.module("ui.bootstrap.tpls", ["template/accordion/accordion-group.html","template/accordion/accordion.html","template/alert/alert.html","template/carousel/carousel.html","template/carousel/slide.html","template/datepicker/datepicker.html","template/datepicker/popup.html","template/modal/backdrop.html","template/modal/window.html","template/pagination/pager.html","template/pagination/pagination.html","template/tooltip/tooltip-html-unsafe-popup.html","template/tooltip/tooltip-popup.html","template/popover/popover.html","template/progressbar/bar.html","template/progressbar/progress.html","template/progressbar/progressbar.html","template/rating/rating.html","template/tabs/tab.html","template/tabs/tabset.html","template/timepicker/timepicker.html","template/typeahead/typeahead-match.html","template/typeahead/typeahead-popup.html"]);
angular.module('ui.bootstrap.transition', [])

/**
 * $transition service provides a consistent interface to trigger CSS 3 transitions and to be informed when they complete.
 * @param  {DOMElement} element  The DOMElement that will be animated.
 * @param  {string|object|function} trigger  The thing that will cause the transition to start:
 *   - As a string, it represents the css class to be added to the element.
 *   - As an object, it represents a hash of style attributes to be applied to the element.
 *   - As a function, it represents a function to be called that will cause the transition to occur.
 * @return {Promise}  A promise that is resolved when the transition finishes.
 */
.factory('$transition', ['$q', '$timeout', '$rootScope', function($q, $timeout, $rootScope) {

  var $transition = function(element, trigger, options) {
    options = options || {};
    var deferred = $q.defer();
    var endEventName = $transition[options.animation ? "animationEndEventName" : "transitionEndEventName"];

    var transitionEndHandler = function(event) {
      $rootScope.$apply(function() {
        element.unbind(endEventName, transitionEndHandler);
        deferred.resolve(element);
      });
    };

    if (endEventName) {
      element.bind(endEventName, transitionEndHandler);
    }

    // Wrap in a timeout to allow the browser time to update the DOM before the transition is to occur
    $timeout(function() {
      if ( angular.isString(trigger) ) {
        element.addClass(trigger);
      } else if ( angular.isFunction(trigger) ) {
        trigger(element);
      } else if ( angular.isObject(trigger) ) {
        element.css(trigger);
      }
      //If browser does not support transitions, instantly resolve
      if ( !endEventName ) {
        deferred.resolve(element);
      }
    });

    // Add our custom cancel function to the promise that is returned
    // We can call this if we are about to run a new transition, which we know will prevent this transition from ending,
    // i.e. it will therefore never raise a transitionEnd event for that transition
    deferred.promise.cancel = function() {
      if ( endEventName ) {
        element.unbind(endEventName, transitionEndHandler);
      }
      deferred.reject('Transition cancelled');
    };

    return deferred.promise;
  };

  // Work out the name of the transitionEnd event
  var transElement = document.createElement('trans');
  var transitionEndEventNames = {
    'WebkitTransition': 'webkitTransitionEnd',
    'MozTransition': 'transitionend',
    'OTransition': 'oTransitionEnd',
    'transition': 'transitionend'
  };
  var animationEndEventNames = {
    'WebkitTransition': 'webkitAnimationEnd',
    'MozTransition': 'animationend',
    'OTransition': 'oAnimationEnd',
    'transition': 'animationend'
  };
  function findEndEventName(endEventNames) {
    for (var name in endEventNames){
      if (transElement.style[name] !== undefined) {
        return endEventNames[name];
      }
    }
  }
  $transition.transitionEndEventName = findEndEventName(transitionEndEventNames);
  $transition.animationEndEventName = findEndEventName(animationEndEventNames);
  return $transition;
}]);

angular.module('ui.bootstrap.collapse', ['ui.bootstrap.transition'])

  .directive('collapse', ['$transition', function ($transition, $timeout) {

    return {
      link: function (scope, element, attrs) {

        var initialAnimSkip = true;
        var currentTransition;

        function doTransition(change) {
          var newTransition = $transition(element, change);
          if (currentTransition) {
            currentTransition.cancel();
          }
          currentTransition = newTransition;
          newTransition.then(newTransitionDone, newTransitionDone);
          return newTransition;

          function newTransitionDone() {
            // Make sure it's this transition, otherwise, leave it alone.
            if (currentTransition === newTransition) {
              currentTransition = undefined;
            }
          }
        }

        function expand() {
          if (initialAnimSkip) {
            initialAnimSkip = false;
            expandDone();
          } else {
            element.removeClass('collapse').addClass('collapsing');
            doTransition({ height: element[0].scrollHeight + 'px' }).then(expandDone);
          }
        }

        function expandDone() {
          element.removeClass('collapsing');
          element.addClass('collapse in');
          element.css({height: 'auto'});
        }

        function collapse() {
          if (initialAnimSkip) {
            initialAnimSkip = false;
            collapseDone();
            element.css({height: 0});
          } else {
            // CSS transitions don't work with height: auto, so we have to manually change the height to a specific value
            element.css({ height: element[0].scrollHeight + 'px' });
            //trigger reflow so a browser relaizes that height was updated from auto to a specific value
            var x = element[0].offsetWidth;

            element.removeClass('collapse in').addClass('collapsing');

            doTransition({ height: 0 }).then(collapseDone);
          }
        }

        function collapseDone() {
          element.removeClass('collapsing');
          element.addClass('collapse');
        }

        scope.$watch(attrs.collapse, function (shouldCollapse) {
          if (shouldCollapse) {
            collapse();
          } else {
            expand();
          }
        });
      }
    };
  }]);

angular.module('ui.bootstrap.accordion', ['ui.bootstrap.collapse'])

.constant('accordionConfig', {
  closeOthers: true
})

.controller('AccordionController', ['$scope', '$attrs', 'accordionConfig', function ($scope, $attrs, accordionConfig) {

  // This array keeps track of the accordion groups
  this.groups = [];

  // Ensure that all the groups in this accordion are closed, unless close-others explicitly says not to
  this.closeOthers = function(openGroup) {
    var closeOthers = angular.isDefined($attrs.closeOthers) ? $scope.$eval($attrs.closeOthers) : accordionConfig.closeOthers;
    if ( closeOthers ) {
      angular.forEach(this.groups, function (group) {
        if ( group !== openGroup ) {
          group.isOpen = false;
        }
      });
    }
  };
  
  // This is called from the accordion-group directive to add itself to the accordion
  this.addGroup = function(groupScope) {
    var that = this;
    this.groups.push(groupScope);

    groupScope.$on('$destroy', function (event) {
      that.removeGroup(groupScope);
    });
  };

  // This is called from the accordion-group directive when to remove itself
  this.removeGroup = function(group) {
    var index = this.groups.indexOf(group);
    if ( index !== -1 ) {
      this.groups.splice(this.groups.indexOf(group), 1);
    }
  };

}])

// The accordion directive simply sets up the directive controller
// and adds an accordion CSS class to itself element.
.directive('accordion', function () {
  return {
    restrict:'EA',
    controller:'AccordionController',
    transclude: true,
    replace: false,
    templateUrl: 'template/accordion/accordion.html'
  };
})

// The accordion-group directive indicates a block of html that will expand and collapse in an accordion
.directive('accordionGroup', ['$parse', function($parse) {
  return {
    require:'^accordion',         // We need this directive to be inside an accordion
    restrict:'EA',
    transclude:true,              // It transcludes the contents of the directive into the template
    replace: true,                // The element containing the directive will be replaced with the template
    templateUrl:'template/accordion/accordion-group.html',
    scope:{ heading:'@' },        // Create an isolated scope and interpolate the heading attribute onto this scope
    controller: function() {
      this.setHeading = function(element) {
        this.heading = element;
      };
    },
    link: function(scope, element, attrs, accordionCtrl) {
      var getIsOpen, setIsOpen;

      accordionCtrl.addGroup(scope);

      scope.isOpen = false;
      
      if ( attrs.isOpen ) {
        getIsOpen = $parse(attrs.isOpen);
        setIsOpen = getIsOpen.assign;

        scope.$parent.$watch(getIsOpen, function(value) {
          scope.isOpen = !!value;
        });
      }

      scope.$watch('isOpen', function(value) {
        if ( value ) {
          accordionCtrl.closeOthers(scope);
        }
        if ( setIsOpen ) {
          setIsOpen(scope.$parent, value);
        }
      });
    }
  };
}])

// Use accordion-heading below an accordion-group to provide a heading containing HTML
// <accordion-group>
//   <accordion-heading>Heading containing HTML - <img src="..."></accordion-heading>
// </accordion-group>
.directive('accordionHeading', function() {
  return {
    restrict: 'EA',
    transclude: true,   // Grab the contents to be used as the heading
    template: '',       // In effect remove this element!
    replace: true,
    require: '^accordionGroup',
    compile: function(element, attr, transclude) {
      return function link(scope, element, attr, accordionGroupCtrl) {
        // Pass the heading to the accordion-group controller
        // so that it can be transcluded into the right place in the template
        // [The second parameter to transclude causes the elements to be cloned so that they work in ng-repeat]
        accordionGroupCtrl.setHeading(transclude(scope, function() {}));
      };
    }
  };
})

// Use in the accordion-group template to indicate where you want the heading to be transcluded
// You must provide the property on the accordion-group controller that will hold the transcluded element
// <div class="accordion-group">
//   <div class="accordion-heading" ><a ... accordion-transclude="heading">...</a></div>
//   ...
// </div>
.directive('accordionTransclude', function() {
  return {
    require: '^accordionGroup',
    link: function(scope, element, attr, controller) {
      scope.$watch(function() { return controller[attr.accordionTransclude]; }, function(heading) {
        if ( heading ) {
          element.html('');
          element.append(heading);
        }
      });
    }
  };
});

angular.module("ui.bootstrap.alert", [])

.controller('AlertController', ['$scope', '$attrs', function ($scope, $attrs) {
  $scope.closeable = 'close' in $attrs;
}])

.directive('alert', function () {
  return {
    restrict:'EA',
    controller:'AlertController',
    templateUrl:'template/alert/alert.html',
    transclude:true,
    replace:true,
    scope: {
      type: '=',
      close: '&'
    }
  };
});

angular.module('ui.bootstrap.bindHtml', [])

  .directive('bindHtmlUnsafe', function () {
    return function (scope, element, attr) {
      element.addClass('ng-binding').data('$binding', attr.bindHtmlUnsafe);
      scope.$watch(attr.bindHtmlUnsafe, function bindHtmlUnsafeWatchAction(value) {
        element.html(value || '');
      });
    };
  });
angular.module('ui.bootstrap.buttons', [])

.constant('buttonConfig', {
  activeClass: 'active',
  toggleEvent: 'click'
})

.controller('ButtonsController', ['buttonConfig', function(buttonConfig) {
  this.activeClass = buttonConfig.activeClass || 'active';
  this.toggleEvent = buttonConfig.toggleEvent || 'click';
}])

.directive('btnRadio', function () {
  return {
    require: ['btnRadio', 'ngModel'],
    controller: 'ButtonsController',
    link: function (scope, element, attrs, ctrls) {
      var buttonsCtrl = ctrls[0], ngModelCtrl = ctrls[1];

      //model -> UI
      ngModelCtrl.$render = function () {
        element.toggleClass(buttonsCtrl.activeClass, angular.equals(ngModelCtrl.$modelValue, scope.$eval(attrs.btnRadio)));
      };

      //ui->model
      element.bind(buttonsCtrl.toggleEvent, function () {
        if (!element.hasClass(buttonsCtrl.activeClass)) {
          scope.$apply(function () {
            ngModelCtrl.$setViewValue(scope.$eval(attrs.btnRadio));
            ngModelCtrl.$render();
          });
        }
      });
    }
  };
})

.directive('btnCheckbox', function () {
  return {
    require: ['btnCheckbox', 'ngModel'],
    controller: 'ButtonsController',
    link: function (scope, element, attrs, ctrls) {
      var buttonsCtrl = ctrls[0], ngModelCtrl = ctrls[1];

      function getTrueValue() {
        return getCheckboxValue(attrs.btnCheckboxTrue, true);
      }

      function getFalseValue() {
        return getCheckboxValue(attrs.btnCheckboxFalse, false);
      }
      
      function getCheckboxValue(attributeValue, defaultValue) {
        var val = scope.$eval(attributeValue);
        return angular.isDefined(val) ? val : defaultValue;
      }

      //model -> UI
      ngModelCtrl.$render = function () {
        element.toggleClass(buttonsCtrl.activeClass, angular.equals(ngModelCtrl.$modelValue, getTrueValue()));
      };

      //ui->model
      element.bind(buttonsCtrl.toggleEvent, function () {
        scope.$apply(function () {
          ngModelCtrl.$setViewValue(element.hasClass(buttonsCtrl.activeClass) ? getFalseValue() : getTrueValue());
          ngModelCtrl.$render();
        });
      });
    }
  };
});

/**
* @ngdoc overview
* @name ui.bootstrap.carousel
*
* @description
* AngularJS version of an image carousel.
*
*/
angular.module('ui.bootstrap.carousel', ['ui.bootstrap.transition'])
.controller('CarouselController', ['$scope', '$timeout', '$transition', '$q', function ($scope, $timeout, $transition, $q) {
  var self = this,
    slides = self.slides = [],
    currentIndex = -1,
    currentTimeout, isPlaying;
  self.currentSlide = null;

  var destroyed = false;
  /* direction: "prev" or "next" */
  self.select = function(nextSlide, direction) {
    var nextIndex = slides.indexOf(nextSlide);
    //Decide direction if it's not given
    if (direction === undefined) {
      direction = nextIndex > currentIndex ? "next" : "prev";
    }
    if (nextSlide && nextSlide !== self.currentSlide) {
      if ($scope.$currentTransition) {
        $scope.$currentTransition.cancel();
        //Timeout so ng-class in template has time to fix classes for finished slide
        $timeout(goNext);
      } else {
        goNext();
      }
    }
    function goNext() {
      // Scope has been destroyed, stop here.
      if (destroyed) { return; }
      //If we have a slide to transition from and we have a transition type and we're allowed, go
      if (self.currentSlide && angular.isString(direction) && !$scope.noTransition && nextSlide.$element) {
        //We shouldn't do class manip in here, but it's the same weird thing bootstrap does. need to fix sometime
        nextSlide.$element.addClass(direction);
        var reflow = nextSlide.$element[0].offsetWidth; //force reflow

        //Set all other slides to stop doing their stuff for the new transition
        angular.forEach(slides, function(slide) {
          angular.extend(slide, {direction: '', entering: false, leaving: false, active: false});
        });
        angular.extend(nextSlide, {direction: direction, active: true, entering: true});
        angular.extend(self.currentSlide||{}, {direction: direction, leaving: true});

        $scope.$currentTransition = $transition(nextSlide.$element, {});
        //We have to create new pointers inside a closure since next & current will change
        (function(next,current) {
          $scope.$currentTransition.then(
            function(){ transitionDone(next, current); },
            function(){ transitionDone(next, current); }
          );
        }(nextSlide, self.currentSlide));
      } else {
        transitionDone(nextSlide, self.currentSlide);
      }
      self.currentSlide = nextSlide;
      currentIndex = nextIndex;
      //every time you change slides, reset the timer
      restartTimer();
    }
    function transitionDone(next, current) {
      angular.extend(next, {direction: '', active: true, leaving: false, entering: false});
      angular.extend(current||{}, {direction: '', active: false, leaving: false, entering: false});
      $scope.$currentTransition = null;
    }
  };
  $scope.$on('$destroy', function () {
    destroyed = true;
  });

  /* Allow outside people to call indexOf on slides array */
  self.indexOfSlide = function(slide) {
    return slides.indexOf(slide);
  };

  $scope.next = function() {
    var newIndex = (currentIndex + 1) % slides.length;

    //Prevent this user-triggered transition from occurring if there is already one in progress
    if (!$scope.$currentTransition) {
      return self.select(slides[newIndex], 'next');
    }
  };

  $scope.prev = function() {
    var newIndex = currentIndex - 1 < 0 ? slides.length - 1 : currentIndex - 1;

    //Prevent this user-triggered transition from occurring if there is already one in progress
    if (!$scope.$currentTransition) {
      return self.select(slides[newIndex], 'prev');
    }
  };

  $scope.select = function(slide) {
    self.select(slide);
  };

  $scope.isActive = function(slide) {
     return self.currentSlide === slide;
  };

  $scope.slides = function() {
    return slides;
  };

  $scope.$watch('interval', restartTimer);
  $scope.$on('$destroy', resetTimer);

  function restartTimer() {
    resetTimer();
    var interval = +$scope.interval;
    if (!isNaN(interval) && interval>=0) {
      currentTimeout = $timeout(timerFn, interval);
    }
  }

  function resetTimer() {
    if (currentTimeout) {
      $timeout.cancel(currentTimeout);
      currentTimeout = null;
    }
  }

  function timerFn() {
    if (isPlaying) {
      $scope.next();
      restartTimer();
    } else {
      $scope.pause();
    }
  }

  $scope.play = function() {
    if (!isPlaying) {
      isPlaying = true;
      restartTimer();
    }
  };
  $scope.pause = function() {
    if (!$scope.noPause) {
      isPlaying = false;
      resetTimer();
    }
  };

  self.addSlide = function(slide, element) {
    slide.$element = element;
    slides.push(slide);
    //if this is the first slide or the slide is set to active, select it
    if(slides.length === 1 || slide.active) {
      self.select(slides[slides.length-1]);
      if (slides.length == 1) {
        $scope.play();
      }
    } else {
      slide.active = false;
    }
  };

  self.removeSlide = function(slide) {
    //get the index of the slide inside the carousel
    var index = slides.indexOf(slide);
    slides.splice(index, 1);
    if (slides.length > 0 && slide.active) {
      if (index >= slides.length) {
        self.select(slides[index-1]);
      } else {
        self.select(slides[index]);
      }
    } else if (currentIndex > index) {
      currentIndex--;
    }
  };

}])

/**
 * @ngdoc directive
 * @name ui.bootstrap.carousel.directive:carousel
 * @restrict EA
 *
 * @description
 * Carousel is the outer container for a set of image 'slides' to showcase.
 *
 * @param {number=} interval The time, in milliseconds, that it will take the carousel to go to the next slide.
 * @param {boolean=} noTransition Whether to disable transitions on the carousel.
 * @param {boolean=} noPause Whether to disable pausing on the carousel (by default, the carousel interval pauses on hover).
 *
 * @example
<example module="ui.bootstrap">
  <file name="index.html">
    <carousel>
      <slide>
        <img src="http://placekitten.com/150/150" style="margin:auto;">
        <div class="carousel-caption">
          <p>Beautiful!</p>
        </div>
      </slide>
      <slide>
        <img src="http://placekitten.com/100/150" style="margin:auto;">
        <div class="carousel-caption">
          <p>D'aww!</p>
        </div>
      </slide>
    </carousel>
  </file>
  <file name="demo.css">
    .carousel-indicators {
      top: auto;
      bottom: 15px;
    }
  </file>
</example>
 */
.directive('carousel', [function() {
  return {
    restrict: 'EA',
    transclude: true,
    replace: true,
    controller: 'CarouselController',
    require: 'carousel',
    templateUrl: 'template/carousel/carousel.html',
    scope: {
      interval: '=',
      noTransition: '=',
      noPause: '='
    }
  };
}])

/**
 * @ngdoc directive
 * @name ui.bootstrap.carousel.directive:slide
 * @restrict EA
 *
 * @description
 * Creates a slide inside a {@link ui.bootstrap.carousel.directive:carousel carousel}.  Must be placed as a child of a carousel element.
 *
 * @param {boolean=} active Model binding, whether or not this slide is currently active.
 *
 * @example
<example module="ui.bootstrap">
  <file name="index.html">
<div ng-controller="CarouselDemoCtrl">
  <carousel>
    <slide ng-repeat="slide in slides" active="slide.active">
      <img ng-src="{{slide.image}}" style="margin:auto;">
      <div class="carousel-caption">
        <h4>Slide {{$index}}</h4>
        <p>{{slide.text}}</p>
      </div>
    </slide>
  </carousel>
  <div class="row-fluid">
    <div class="span6">
      <ul>
        <li ng-repeat="slide in slides">
          <button class="btn btn-mini" ng-class="{'btn-info': !slide.active, 'btn-success': slide.active}" ng-disabled="slide.active" ng-click="slide.active = true">select</button>
          {{$index}}: {{slide.text}}
        </li>
      </ul>
      <a class="btn" ng-click="addSlide()">Add Slide</a>
    </div>
    <div class="span6">
      Interval, in milliseconds: <input type="number" ng-model="myInterval">
      <br />Enter a negative number to stop the interval.
    </div>
  </div>
</div>
  </file>
  <file name="script.js">
function CarouselDemoCtrl($scope) {
  $scope.myInterval = 5000;
  var slides = $scope.slides = [];
  $scope.addSlide = function() {
    var newWidth = 200 + ((slides.length + (25 * slides.length)) % 150);
    slides.push({
      image: 'http://placekitten.com/' + newWidth + '/200',
      text: ['More','Extra','Lots of','Surplus'][slides.length % 4] + ' '
        ['Cats', 'Kittys', 'Felines', 'Cutes'][slides.length % 4]
    });
  };
  for (var i=0; i<4; i++) $scope.addSlide();
}
  </file>
  <file name="demo.css">
    .carousel-indicators {
      top: auto;
      bottom: 15px;
    }
  </file>
</example>
*/

.directive('slide', ['$parse', function($parse) {
  return {
    require: '^carousel',
    restrict: 'EA',
    transclude: true,
    replace: true,
    templateUrl: 'template/carousel/slide.html',
    scope: {
    },
    link: function (scope, element, attrs, carouselCtrl) {
      //Set up optional 'active' = binding
      if (attrs.active) {
        var getActive = $parse(attrs.active);
        var setActive = getActive.assign;
        var lastValue = scope.active = getActive(scope.$parent);
        scope.$watch(function parentActiveWatch() {
          var parentActive = getActive(scope.$parent);

          if (parentActive !== scope.active) {
            // we are out of sync and need to copy
            if (parentActive !== lastValue) {
              // parent changed and it has precedence
              lastValue = scope.active = parentActive;
            } else {
              // if the parent can be assigned then do so
              setActive(scope.$parent, parentActive = lastValue = scope.active);
            }
          }
          return parentActive;
        });
      }

      carouselCtrl.addSlide(scope, element);
      //when the scope is destroyed then remove the slide from the current slides array
      scope.$on('$destroy', function() {
        carouselCtrl.removeSlide(scope);
      });

      scope.$watch('active', function(active) {
        if (active) {
          carouselCtrl.select(scope);
        }
      });
    }
  };
}]);

angular.module('ui.bootstrap.position', [])

/**
 * A set of utility methods that can be use to retrieve position of DOM elements.
 * It is meant to be used where we need to absolute-position DOM elements in
 * relation to other, existing elements (this is the case for tooltips, popovers,
 * typeahead suggestions etc.).
 */
  .factory('$position', ['$document', '$window', function ($document, $window) {

    function getStyle(el, cssprop) {
      if (el.currentStyle) { //IE
        return el.currentStyle[cssprop];
      } else if ($window.getComputedStyle) {
        return $window.getComputedStyle(el)[cssprop];
      }
      // finally try and get inline style
      return el.style[cssprop];
    }

    /**
     * Checks if a given element is statically positioned
     * @param element - raw DOM element
     */
    function isStaticPositioned(element) {
      return (getStyle(element, "position") || 'static' ) === 'static';
    }

    /**
     * returns the closest, non-statically positioned parentOffset of a given element
     * @param element
     */
    var parentOffsetEl = function (element) {
      var docDomEl = $document[0];
      var offsetParent = element.offsetParent || docDomEl;
      while (offsetParent && offsetParent !== docDomEl && isStaticPositioned(offsetParent) ) {
        offsetParent = offsetParent.offsetParent;
      }
      return offsetParent || docDomEl;
    };

    return {
      /**
       * Provides read-only equivalent of jQuery's position function:
       * http://api.jquery.com/position/
       */
      position: function (element) {
        var elBCR = this.offset(element);
        var offsetParentBCR = { top: 0, left: 0 };
        var offsetParentEl = parentOffsetEl(element[0]);
        if (offsetParentEl != $document[0]) {
          offsetParentBCR = this.offset(angular.element(offsetParentEl));
          offsetParentBCR.top += offsetParentEl.clientTop - offsetParentEl.scrollTop;
          offsetParentBCR.left += offsetParentEl.clientLeft - offsetParentEl.scrollLeft;
        }

        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: boundingClientRect.width || element.prop('offsetWidth'),
          height: boundingClientRect.height || element.prop('offsetHeight'),
          top: elBCR.top - offsetParentBCR.top,
          left: elBCR.left - offsetParentBCR.left
        };
      },

      /**
       * Provides read-only equivalent of jQuery's offset function:
       * http://api.jquery.com/offset/
       */
      offset: function (element) {
        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: boundingClientRect.width || element.prop('offsetWidth'),
          height: boundingClientRect.height || element.prop('offsetHeight'),
          top: boundingClientRect.top + ($window.pageYOffset || $document[0].body.scrollTop || $document[0].documentElement.scrollTop),
          left: boundingClientRect.left + ($window.pageXOffset || $document[0].body.scrollLeft  || $document[0].documentElement.scrollLeft)
        };
      }
    };
  }]);

angular.module('ui.bootstrap.datepicker', ['ui.bootstrap.position'])

.constant('datepickerConfig', {
  dayFormat: 'dd',
  monthFormat: 'MMMM',
  yearFormat: 'yyyy',
  dayHeaderFormat: 'EEE',
  dayTitleFormat: 'MMMM yyyy',
  monthTitleFormat: 'yyyy',
  showWeeks: true,
  startingDay: 0,
  yearRange: 20,
  minDate: null,
  maxDate: null
})

.controller('DatepickerController', ['$scope', '$attrs', 'dateFilter', 'datepickerConfig', function($scope, $attrs, dateFilter, dtConfig) {
  var format = {
    day:        getValue($attrs.dayFormat,        dtConfig.dayFormat),
    month:      getValue($attrs.monthFormat,      dtConfig.monthFormat),
    year:       getValue($attrs.yearFormat,       dtConfig.yearFormat),
    dayHeader:  getValue($attrs.dayHeaderFormat,  dtConfig.dayHeaderFormat),
    dayTitle:   getValue($attrs.dayTitleFormat,   dtConfig.dayTitleFormat),
    monthTitle: getValue($attrs.monthTitleFormat, dtConfig.monthTitleFormat)
  },
  startingDay = getValue($attrs.startingDay,      dtConfig.startingDay),
  yearRange =   getValue($attrs.yearRange,        dtConfig.yearRange);

  this.minDate = dtConfig.minDate ? new Date(dtConfig.minDate) : null;
  this.maxDate = dtConfig.maxDate ? new Date(dtConfig.maxDate) : null;

  function getValue(value, defaultValue) {
    return angular.isDefined(value) ? $scope.$parent.$eval(value) : defaultValue;
  }

  function getDaysInMonth( year, month ) {
    return new Date(year, month, 0).getDate();
  }

  function getDates(startDate, n) {
    var dates = new Array(n);
    var current = startDate, i = 0;
    while (i < n) {
      dates[i++] = new Date(current);
      current.setDate( current.getDate() + 1 );
    }
    return dates;
  }

  function makeDate(date, format, isSelected, isSecondary) {
    return { date: date, label: dateFilter(date, format), selected: !!isSelected, secondary: !!isSecondary };
  }

  this.modes = [
    {
      name: 'day',
      getVisibleDates: function(date, selected) {
        var year = date.getFullYear(), month = date.getMonth(), firstDayOfMonth = new Date(year, month, 1);
        var difference = startingDay - firstDayOfMonth.getDay(),
        numDisplayedFromPreviousMonth = (difference > 0) ? 7 - difference : - difference,
        firstDate = new Date(firstDayOfMonth), numDates = 0;

        if ( numDisplayedFromPreviousMonth > 0 ) {
          firstDate.setDate( - numDisplayedFromPreviousMonth + 1 );
          numDates += numDisplayedFromPreviousMonth; // Previous
        }
        numDates += getDaysInMonth(year, month + 1); // Current
        numDates += (7 - numDates % 7) % 7; // Next

        var days = getDates(firstDate, numDates), labels = new Array(7);
        for (var i = 0; i < numDates; i ++) {
          var dt = new Date(days[i]);
          days[i] = makeDate(dt, format.day, (selected && selected.getDate() === dt.getDate() && selected.getMonth() === dt.getMonth() && selected.getFullYear() === dt.getFullYear()), dt.getMonth() !== month);
        }
        for (var j = 0; j < 7; j++) {
          labels[j] = dateFilter(days[j].date, format.dayHeader);
        }
        return { objects: days, title: dateFilter(date, format.dayTitle), labels: labels };
      },
      compare: function(date1, date2) {
        return (new Date( date1.getFullYear(), date1.getMonth(), date1.getDate() ) - new Date( date2.getFullYear(), date2.getMonth(), date2.getDate() ) );
      },
      split: 7,
      step: { months: 1 }
    },
    {
      name: 'month',
      getVisibleDates: function(date, selected) {
        var months = new Array(12), year = date.getFullYear();
        for ( var i = 0; i < 12; i++ ) {
          var dt = new Date(year, i, 1);
          months[i] = makeDate(dt, format.month, (selected && selected.getMonth() === i && selected.getFullYear() === year));
        }
        return { objects: months, title: dateFilter(date, format.monthTitle) };
      },
      compare: function(date1, date2) {
        return new Date( date1.getFullYear(), date1.getMonth() ) - new Date( date2.getFullYear(), date2.getMonth() );
      },
      split: 3,
      step: { years: 1 }
    },
    {
      name: 'year',
      getVisibleDates: function(date, selected) {
        var years = new Array(yearRange), year = date.getFullYear(), startYear = parseInt((year - 1) / yearRange, 10) * yearRange + 1;
        for ( var i = 0; i < yearRange; i++ ) {
          var dt = new Date(startYear + i, 0, 1);
          years[i] = makeDate(dt, format.year, (selected && selected.getFullYear() === dt.getFullYear()));
        }
        return { objects: years, title: [years[0].label, years[yearRange - 1].label].join(' - ') };
      },
      compare: function(date1, date2) {
        return date1.getFullYear() - date2.getFullYear();
      },
      split: 5,
      step: { years: yearRange }
    }
  ];

  this.isDisabled = function(date, mode) {
    var currentMode = this.modes[mode || 0];
    return ((this.minDate && currentMode.compare(date, this.minDate) < 0) || (this.maxDate && currentMode.compare(date, this.maxDate) > 0) || ($scope.dateDisabled && $scope.dateDisabled({date: date, mode: currentMode.name})));
  };
}])

.directive( 'datepicker', ['dateFilter', '$parse', 'datepickerConfig', '$log', function (dateFilter, $parse, datepickerConfig, $log) {
  return {
    restrict: 'EA',
    replace: true,
    templateUrl: 'template/datepicker/datepicker.html',
    scope: {
      dateDisabled: '&'
    },
    require: ['datepicker', '?^ngModel'],
    controller: 'DatepickerController',
    link: function(scope, element, attrs, ctrls) {
      var datepickerCtrl = ctrls[0], ngModel = ctrls[1];

      if (!ngModel) {
        return; // do nothing if no ng-model
      }

      // Configuration parameters
      var mode = 0, selected = new Date(), showWeeks = datepickerConfig.showWeeks;

      if (attrs.showWeeks) {
        scope.$parent.$watch($parse(attrs.showWeeks), function(value) {
          showWeeks = !! value;
          updateShowWeekNumbers();
        });
      } else {
        updateShowWeekNumbers();
      }

      if (attrs.min) {
        scope.$parent.$watch($parse(attrs.min), function(value) {
          datepickerCtrl.minDate = value ? new Date(value) : null;
          refill();
        });
      }
      if (attrs.max) {
        scope.$parent.$watch($parse(attrs.max), function(value) {
          datepickerCtrl.maxDate = value ? new Date(value) : null;
          refill();
        });
      }

      function updateShowWeekNumbers() {
        scope.showWeekNumbers = mode === 0 && showWeeks;
      }

      // Split array into smaller arrays
      function split(arr, size) {
        var arrays = [];
        while (arr.length > 0) {
          arrays.push(arr.splice(0, size));
        }
        return arrays;
      }

      function refill( updateSelected ) {
        var date = null, valid = true;

        if ( ngModel.$modelValue ) {
          date = new Date( ngModel.$modelValue );

          if ( isNaN(date) ) {
            valid = false;
            $log.error('Datepicker directive: "ng-model" value must be a Date object, a number of milliseconds since 01.01.1970 or a string representing an RFC2822 or ISO 8601 date.');
          } else if ( updateSelected ) {
            selected = date;
          }
        }
        ngModel.$setValidity('date', valid);

        var currentMode = datepickerCtrl.modes[mode], data = currentMode.getVisibleDates(selected, date);
        angular.forEach(data.objects, function(obj) {
          obj.disabled = datepickerCtrl.isDisabled(obj.date, mode);
        });

        ngModel.$setValidity('date-disabled', (!date || !datepickerCtrl.isDisabled(date)));

        scope.rows = split(data.objects, currentMode.split);
        scope.labels = data.labels || [];
        scope.title = data.title;
      }

      function setMode(value) {
        mode = value;
        updateShowWeekNumbers();
        refill();
      }

      ngModel.$render = function() {
        refill( true );
      };

      scope.select = function( date ) {
        if ( mode === 0 ) {
          var dt = ngModel.$modelValue ? new Date( ngModel.$modelValue ) : new Date(0, 0, 0, 0, 0, 0, 0);
          dt.setFullYear( date.getFullYear(), date.getMonth(), date.getDate() );
          ngModel.$setViewValue( dt );
          refill( true );
        } else {
          selected = date;
          setMode( mode - 1 );
        }
      };
      scope.move = function(direction) {
        var step = datepickerCtrl.modes[mode].step;
        selected.setMonth( selected.getMonth() + direction * (step.months || 0) );
        selected.setFullYear( selected.getFullYear() + direction * (step.years || 0) );
        refill();
      };
      scope.toggleMode = function() {
        setMode( (mode + 1) % datepickerCtrl.modes.length );
      };
      scope.getWeekNumber = function(row) {
        return ( mode === 0 && scope.showWeekNumbers && row.length === 7 ) ? getISO8601WeekNumber(row[0].date) : null;
      };

      function getISO8601WeekNumber(date) {
        var checkDate = new Date(date);
        checkDate.setDate(checkDate.getDate() + 4 - (checkDate.getDay() || 7)); // Thursday
        var time = checkDate.getTime();
        checkDate.setMonth(0); // Compare with Jan 1
        checkDate.setDate(1);
        return Math.floor(Math.round((time - checkDate) / 86400000) / 7) + 1;
      }
    }
  };
}])

.constant('datepickerPopupConfig', {
  dateFormat: 'yyyy-MM-dd',
  currentText: 'Today',
  toggleWeeksText: 'Weeks',
  clearText: 'Clear',
  closeText: 'Done',
  closeOnDateSelection: true,
  appendToBody: false,
  showButtonBar: true
})

.directive('datepickerPopup', ['$compile', '$parse', '$document', '$position', 'dateFilter', 'datepickerPopupConfig', 'datepickerConfig',
function ($compile, $parse, $document, $position, dateFilter, datepickerPopupConfig, datepickerConfig) {
  return {
    restrict: 'EA',
    require: 'ngModel',
    link: function(originalScope, element, attrs, ngModel) {
      var scope = originalScope.$new(), // create a child scope so we are not polluting original one
          dateFormat,
          closeOnDateSelection = angular.isDefined(attrs.closeOnDateSelection) ? originalScope.$eval(attrs.closeOnDateSelection) : datepickerPopupConfig.closeOnDateSelection,
          appendToBody = angular.isDefined(attrs.datepickerAppendToBody) ? originalScope.$eval(attrs.datepickerAppendToBody) : datepickerPopupConfig.appendToBody;

      attrs.$observe('datepickerPopup', function(value) {
          dateFormat = value || datepickerPopupConfig.dateFormat;
          ngModel.$render();
      });

      scope.showButtonBar = angular.isDefined(attrs.showButtonBar) ? originalScope.$eval(attrs.showButtonBar) : datepickerPopupConfig.showButtonBar;

      originalScope.$on('$destroy', function() {
        $popup.remove();
        scope.$destroy();
      });

      attrs.$observe('currentText', function(text) {
        scope.currentText = angular.isDefined(text) ? text : datepickerPopupConfig.currentText;
      });
      attrs.$observe('toggleWeeksText', function(text) {
        scope.toggleWeeksText = angular.isDefined(text) ? text : datepickerPopupConfig.toggleWeeksText;
      });
      attrs.$observe('clearText', function(text) {
        scope.clearText = angular.isDefined(text) ? text : datepickerPopupConfig.clearText;
      });
      attrs.$observe('closeText', function(text) {
        scope.closeText = angular.isDefined(text) ? text : datepickerPopupConfig.closeText;
      });

      var getIsOpen, setIsOpen;
      if ( attrs.isOpen ) {
        getIsOpen = $parse(attrs.isOpen);
        setIsOpen = getIsOpen.assign;

        originalScope.$watch(getIsOpen, function updateOpen(value) {
          scope.isOpen = !! value;
        });
      }
      scope.isOpen = getIsOpen ? getIsOpen(originalScope) : false; // Initial state

      function setOpen( value ) {
        if (setIsOpen) {
          setIsOpen(originalScope, !!value);
        } else {
          scope.isOpen = !!value;
        }
      }

      var documentClickBind = function(event) {
        if (scope.isOpen && event.target !== element[0]) {
          scope.$apply(function() {
            setOpen(false);
          });
        }
      };

      var elementFocusBind = function() {
        scope.$apply(function() {
          setOpen( true );
        });
      };

      // popup element used to display calendar
      var popupEl = angular.element('<div datepicker-popup-wrap><div datepicker></div></div>');
      popupEl.attr({
        'ng-model': 'date',
        'ng-change': 'dateSelection()'
      });
      var datepickerEl = angular.element(popupEl.children()[0]);
      if (attrs.datepickerOptions) {
        datepickerEl.attr(angular.extend({}, originalScope.$eval(attrs.datepickerOptions)));
      }

      // TODO: reverse from dateFilter string to Date object
      function parseDate(viewValue) {
        if (!viewValue) {
          ngModel.$setValidity('date', true);
          return null;
        } else if (angular.isDate(viewValue)) {
          ngModel.$setValidity('date', true);
          return viewValue;
        } else if (angular.isString(viewValue)) {
          var date = new Date(viewValue);
          if (isNaN(date)) {
            ngModel.$setValidity('date', false);
            return undefined;
          } else {
            ngModel.$setValidity('date', true);
            return date;
          }
        } else {
          ngModel.$setValidity('date', false);
          return undefined;
        }
      }
      ngModel.$parsers.unshift(parseDate);

      // Inner change
      scope.dateSelection = function(dt) {
        if (angular.isDefined(dt)) {
          scope.date = dt;
        }
        ngModel.$setViewValue(scope.date);
        ngModel.$render();

        if (closeOnDateSelection) {
          setOpen( false );
        }
      };

      element.bind('input change keyup', function() {
        scope.$apply(function() {
          scope.date = ngModel.$modelValue;
        });
      });

      // Outter change
      ngModel.$render = function() {
        var date = ngModel.$viewValue ? dateFilter(ngModel.$viewValue, dateFormat) : '';
        element.val(date);
        scope.date = ngModel.$modelValue;
      };

      function addWatchableAttribute(attribute, scopeProperty, datepickerAttribute) {
        if (attribute) {
          originalScope.$watch($parse(attribute), function(value){
            scope[scopeProperty] = value;
          });
          datepickerEl.attr(datepickerAttribute || scopeProperty, scopeProperty);
        }
      }
      addWatchableAttribute(attrs.min, 'min');
      addWatchableAttribute(attrs.max, 'max');
      if (attrs.showWeeks) {
        addWatchableAttribute(attrs.showWeeks, 'showWeeks', 'show-weeks');
      } else {
        scope.showWeeks = datepickerConfig.showWeeks;
        datepickerEl.attr('show-weeks', 'showWeeks');
      }
      if (attrs.dateDisabled) {
        datepickerEl.attr('date-disabled', attrs.dateDisabled);
      }

      function updatePosition() {
        scope.position = appendToBody ? $position.offset(element) : $position.position(element);
        scope.position.top = scope.position.top + element.prop('offsetHeight');
      }

      var documentBindingInitialized = false, elementFocusInitialized = false;
      scope.$watch('isOpen', function(value) {
        if (value) {
          updatePosition();
          $document.bind('click', documentClickBind);
          if(elementFocusInitialized) {
            element.unbind('focus', elementFocusBind);
          }
          element[0].focus();
          documentBindingInitialized = true;
        } else {
          if(documentBindingInitialized) {
            $document.unbind('click', documentClickBind);
          }
          element.bind('focus', elementFocusBind);
          elementFocusInitialized = true;
        }

        if ( setIsOpen ) {
          setIsOpen(originalScope, value);
        }
      });

      scope.today = function() {
        scope.dateSelection(new Date());
      };
      scope.clear = function() {
        scope.dateSelection(null);
      };

      var $popup = $compile(popupEl)(scope);
      if ( appendToBody ) {
        $document.find('body').append($popup);
      } else {
        element.after($popup);
      }
    }
  };
}])

.directive('datepickerPopupWrap', function() {
  return {
    restrict:'EA',
    replace: true,
    transclude: true,
    templateUrl: 'template/datepicker/popup.html',
    link:function (scope, element, attrs) {
      element.bind('click', function(event) {
        event.preventDefault();
        event.stopPropagation();
      });
    }
  };
});

/*
 * dropdownToggle - Provides dropdown menu functionality in place of bootstrap js
 * @restrict class or attribute
 * @example:
   <li class="dropdown">
     <a class="dropdown-toggle">My Dropdown Menu</a>
     <ul class="dropdown-menu">
       <li ng-repeat="choice in dropChoices">
         <a ng-href="{{choice.href}}">{{choice.text}}</a>
       </li>
     </ul>
   </li>
 */

angular.module('ui.bootstrap.dropdownToggle', []).directive('dropdownToggle', ['$document', '$location', function ($document, $location) {
  var openElement = null,
      closeMenu   = angular.noop;
  return {
    restrict: 'CA',
    link: function(scope, element, attrs) {
      scope.$watch('$location.path', function() { closeMenu(); });
      element.parent().bind('click', function() { closeMenu(); });
      element.bind('click', function (event) {

        var elementWasOpen = (element === openElement);

        event.preventDefault();
        event.stopPropagation();

        if (!!openElement) {
          closeMenu();
        }

        if (!elementWasOpen && !element.hasClass('disabled') && !element.prop('disabled')) {
          element.parent().addClass('open');
          openElement = element;
          closeMenu = function (event) {
            if (event) {
              event.preventDefault();
              event.stopPropagation();
            }
            $document.unbind('click', closeMenu);
            element.parent().removeClass('open');
            closeMenu = angular.noop;
            openElement = null;
          };
          $document.bind('click', closeMenu);
        }
      });
    }
  };
}]);

angular.module('ui.bootstrap.modal', [])

/**
 * A helper, internal data structure that acts as a map but also allows getting / removing
 * elements in the LIFO order
 */
  .factory('$$stackedMap', function () {
    return {
      createNew: function () {
        var stack = [];

        return {
          add: function (key, value) {
            stack.push({
              key: key,
              value: value
            });
          },
          get: function (key) {
            for (var i = 0; i < stack.length; i++) {
              if (key == stack[i].key) {
                return stack[i];
              }
            }
          },
          keys: function() {
            var keys = [];
            for (var i = 0; i < stack.length; i++) {
              keys.push(stack[i].key);
            }
            return keys;
          },
          top: function () {
            return stack[stack.length - 1];
          },
          remove: function (key) {
            var idx = -1;
            for (var i = 0; i < stack.length; i++) {
              if (key == stack[i].key) {
                idx = i;
                break;
              }
            }
            return stack.splice(idx, 1)[0];
          },
          removeTop: function () {
            return stack.splice(stack.length - 1, 1)[0];
          },
          length: function () {
            return stack.length;
          }
        };
      }
    };
  })

/**
 * A helper directive for the $modal service. It creates a backdrop element.
 */
  .directive('modalBackdrop', ['$timeout', function ($timeout) {
    return {
      restrict: 'EA',
      replace: true,
      templateUrl: 'template/modal/backdrop.html',
      link: function (scope) {

        scope.animate = false;

        //trigger CSS transitions
        $timeout(function () {
          scope.animate = true;
        });
      }
    };
  }])

  .directive('modalWindow', ['$modalStack', '$timeout', function ($modalStack, $timeout) {
    return {
      restrict: 'EA',
      scope: {
        index: '@'
      },
      replace: true,
      transclude: true,
      templateUrl: 'template/modal/window.html',
      link: function (scope, element, attrs) {
        scope.windowClass = attrs.windowClass || '';

        $timeout(function () {
          // trigger CSS transitions
          scope.animate = true;
          // focus a freshly-opened modal
          element[0].focus();
        });

        scope.close = function (evt) {
          var modal = $modalStack.getTop();
          if (modal && modal.value.backdrop && modal.value.backdrop != 'static' && (evt.target === evt.currentTarget)) {
            evt.preventDefault();
            evt.stopPropagation();
            $modalStack.dismiss(modal.key, 'backdrop click');
          }
        };
      }
    };
  }])

  .factory('$modalStack', ['$document', '$compile', '$rootScope', '$$stackedMap',
    function ($document, $compile, $rootScope, $$stackedMap) {

      var OPENED_MODAL_CLASS = 'modal-open';

      var backdropjqLiteEl, backdropDomEl;
      var backdropScope = $rootScope.$new(true);
      var openedWindows = $$stackedMap.createNew();
      var $modalStack = {};

      function backdropIndex() {
        var topBackdropIndex = -1;
        var opened = openedWindows.keys();
        for (var i = 0; i < opened.length; i++) {
          if (openedWindows.get(opened[i]).value.backdrop) {
            topBackdropIndex = i;
          }
        }
        return topBackdropIndex;
      }

      $rootScope.$watch(backdropIndex, function(newBackdropIndex){
        backdropScope.index = newBackdropIndex;
      });

      function removeModalWindow(modalInstance) {

        var body = $document.find('body').eq(0);
        var modalWindow = openedWindows.get(modalInstance).value;

        //clean up the stack
        openedWindows.remove(modalInstance);

        //remove window DOM element
        modalWindow.modalDomEl.remove();
        body.toggleClass(OPENED_MODAL_CLASS, openedWindows.length() > 0);

        //remove backdrop if no longer needed
        if (backdropDomEl && backdropIndex() == -1) {
          backdropDomEl.remove();
          backdropDomEl = undefined;
        }

        //destroy scope
        modalWindow.modalScope.$destroy();
      }

      $document.bind('keydown', function (evt) {
        var modal;

        if (evt.which === 27) {
          modal = openedWindows.top();
          if (modal && modal.value.keyboard) {
            $rootScope.$apply(function () {
              $modalStack.dismiss(modal.key);
            });
          }
        }
      });

      $modalStack.open = function (modalInstance, modal) {

        openedWindows.add(modalInstance, {
          deferred: modal.deferred,
          modalScope: modal.scope,
          backdrop: modal.backdrop,
          keyboard: modal.keyboard
        });

        var body = $document.find('body').eq(0);

        if (backdropIndex() >= 0 && !backdropDomEl) {
            backdropjqLiteEl = angular.element('<div modal-backdrop></div>');
            backdropDomEl = $compile(backdropjqLiteEl)(backdropScope);
            body.append(backdropDomEl);
        }
          
        var angularDomEl = angular.element('<div modal-window></div>');
        angularDomEl.attr('window-class', modal.windowClass);
        angularDomEl.attr('index', openedWindows.length() - 1);
        angularDomEl.html(modal.content);

        var modalDomEl = $compile(angularDomEl)(modal.scope);
        openedWindows.top().value.modalDomEl = modalDomEl;
        body.append(modalDomEl);
        body.addClass(OPENED_MODAL_CLASS);
      };

      $modalStack.close = function (modalInstance, result) {
        var modalWindow = openedWindows.get(modalInstance).value;
        if (modalWindow) {
          modalWindow.deferred.resolve(result);
          removeModalWindow(modalInstance);
        }
      };

      $modalStack.dismiss = function (modalInstance, reason) {
        var modalWindow = openedWindows.get(modalInstance).value;
        if (modalWindow) {
          modalWindow.deferred.reject(reason);
          removeModalWindow(modalInstance);
        }
      };

      $modalStack.getTop = function () {
        return openedWindows.top();
      };

      return $modalStack;
    }])

  .provider('$modal', function () {

    var $modalProvider = {
      options: {
        backdrop: true, //can be also false or 'static'
        keyboard: true
      },
      $get: ['$injector', '$rootScope', '$q', '$http', '$templateCache', '$controller', '$modalStack',
        function ($injector, $rootScope, $q, $http, $templateCache, $controller, $modalStack) {

          var $modal = {};

          function getTemplatePromise(options) {
            return options.template ? $q.when(options.template) :
              $http.get(options.templateUrl, {cache: $templateCache}).then(function (result) {
                return result.data;
              });
          }

          function getResolvePromises(resolves) {
            var promisesArr = [];
            angular.forEach(resolves, function (value, key) {
              if (angular.isFunction(value) || angular.isArray(value)) {
                promisesArr.push($q.when($injector.invoke(value)));
              }
            });
            return promisesArr;
          }

          $modal.open = function (modalOptions) {

            var modalResultDeferred = $q.defer();
            var modalOpenedDeferred = $q.defer();

            //prepare an instance of a modal to be injected into controllers and returned to a caller
            var modalInstance = {
              result: modalResultDeferred.promise,
              opened: modalOpenedDeferred.promise,
              close: function (result) {
                $modalStack.close(modalInstance, result);
              },
              dismiss: function (reason) {
                $modalStack.dismiss(modalInstance, reason);
              }
            };

            //merge and clean up options
            modalOptions = angular.extend({}, $modalProvider.options, modalOptions);
            modalOptions.resolve = modalOptions.resolve || {};

            //verify options
            if (!modalOptions.template && !modalOptions.templateUrl) {
              throw new Error('One of template or templateUrl options is required.');
            }

            var templateAndResolvePromise =
              $q.all([getTemplatePromise(modalOptions)].concat(getResolvePromises(modalOptions.resolve)));


            templateAndResolvePromise.then(function resolveSuccess(tplAndVars) {

              var modalScope = (modalOptions.scope || $rootScope).$new();
              modalScope.$close = modalInstance.close;
              modalScope.$dismiss = modalInstance.dismiss;

              var ctrlInstance, ctrlLocals = {};
              var resolveIter = 1;

              //controllers
              if (modalOptions.controller) {
                ctrlLocals.$scope = modalScope;
                ctrlLocals.$modalInstance = modalInstance;
                angular.forEach(modalOptions.resolve, function (value, key) {
                  ctrlLocals[key] = tplAndVars[resolveIter++];
                });

                ctrlInstance = $controller(modalOptions.controller, ctrlLocals);
              }

              $modalStack.open(modalInstance, {
                scope: modalScope,
                deferred: modalResultDeferred,
                content: tplAndVars[0],
                backdrop: modalOptions.backdrop,
                keyboard: modalOptions.keyboard,
                windowClass: modalOptions.windowClass
              });

            }, function resolveError(reason) {
              modalResultDeferred.reject(reason);
            });

            templateAndResolvePromise.then(function () {
              modalOpenedDeferred.resolve(true);
            }, function () {
              modalOpenedDeferred.reject(false);
            });

            return modalInstance;
          };

          return $modal;
        }]
    };

    return $modalProvider;
  });

angular.module('ui.bootstrap.pagination', [])

.controller('PaginationController', ['$scope', '$attrs', '$parse', '$interpolate', function ($scope, $attrs, $parse, $interpolate) {
  var self = this,
      setNumPages = $attrs.numPages ? $parse($attrs.numPages).assign : angular.noop;

  this.init = function(defaultItemsPerPage) {
    if ($attrs.itemsPerPage) {
      $scope.$parent.$watch($parse($attrs.itemsPerPage), function(value) {
        self.itemsPerPage = parseInt(value, 10);
        $scope.totalPages = self.calculateTotalPages();
      });
    } else {
      this.itemsPerPage = defaultItemsPerPage;
    }
  };

  this.noPrevious = function() {
    return this.page === 1;
  };
  this.noNext = function() {
    return this.page === $scope.totalPages;
  };

  this.isActive = function(page) {
    return this.page === page;
  };

  this.calculateTotalPages = function() {
    var totalPages = this.itemsPerPage < 1 ? 1 : Math.ceil($scope.totalItems / this.itemsPerPage);
    return Math.max(totalPages || 0, 1);
  };

  this.getAttributeValue = function(attribute, defaultValue, interpolate) {
    return angular.isDefined(attribute) ? (interpolate ? $interpolate(attribute)($scope.$parent) : $scope.$parent.$eval(attribute)) : defaultValue;
  };

  this.render = function() {
    this.page = parseInt($scope.page, 10) || 1;
    if (this.page > 0 && this.page <= $scope.totalPages) {
      $scope.pages = this.getPages(this.page, $scope.totalPages);
    }
  };

  $scope.selectPage = function(page) {
    if ( ! self.isActive(page) && page > 0 && page <= $scope.totalPages) {
      $scope.page = page;
      $scope.onSelectPage({ page: page });
    }
  };

  $scope.$watch('page', function() {
    self.render();
  });

  $scope.$watch('totalItems', function() {
    $scope.totalPages = self.calculateTotalPages();
  });

  $scope.$watch('totalPages', function(value) {
    setNumPages($scope.$parent, value); // Readonly variable

    if ( self.page > value ) {
      $scope.selectPage(value);
    } else {
      self.render();
    }
  });
}])

.constant('paginationConfig', {
  itemsPerPage: 10,
  boundaryLinks: false,
  directionLinks: true,
  firstText: 'First',
  previousText: 'Previous',
  nextText: 'Next',
  lastText: 'Last',
  rotate: true
})

.directive('pagination', ['$parse', 'paginationConfig', function($parse, config) {
  return {
    restrict: 'EA',
    scope: {
      page: '=',
      totalItems: '=',
      onSelectPage:' &'
    },
    controller: 'PaginationController',
    templateUrl: 'template/pagination/pagination.html',
    replace: true,
    link: function(scope, element, attrs, paginationCtrl) {

      // Setup configuration parameters
      var maxSize,
      boundaryLinks  = paginationCtrl.getAttributeValue(attrs.boundaryLinks,  config.boundaryLinks      ),
      directionLinks = paginationCtrl.getAttributeValue(attrs.directionLinks, config.directionLinks     ),
      firstText      = paginationCtrl.getAttributeValue(attrs.firstText,      config.firstText,     true),
      previousText   = paginationCtrl.getAttributeValue(attrs.previousText,   config.previousText,  true),
      nextText       = paginationCtrl.getAttributeValue(attrs.nextText,       config.nextText,      true),
      lastText       = paginationCtrl.getAttributeValue(attrs.lastText,       config.lastText,      true),
      rotate         = paginationCtrl.getAttributeValue(attrs.rotate,         config.rotate);

      paginationCtrl.init(config.itemsPerPage);

      if (attrs.maxSize) {
        scope.$parent.$watch($parse(attrs.maxSize), function(value) {
          maxSize = parseInt(value, 10);
          paginationCtrl.render();
        });
      }

      // Create page object used in template
      function makePage(number, text, isActive, isDisabled) {
        return {
          number: number,
          text: text,
          active: isActive,
          disabled: isDisabled
        };
      }

      paginationCtrl.getPages = function(currentPage, totalPages) {
        var pages = [];

        // Default page limits
        var startPage = 1, endPage = totalPages;
        var isMaxSized = ( angular.isDefined(maxSize) && maxSize < totalPages );

        // recompute if maxSize
        if ( isMaxSized ) {
          if ( rotate ) {
            // Current page is displayed in the middle of the visible ones
            startPage = Math.max(currentPage - Math.floor(maxSize/2), 1);
            endPage   = startPage + maxSize - 1;

            // Adjust if limit is exceeded
            if (endPage > totalPages) {
              endPage   = totalPages;
              startPage = endPage - maxSize + 1;
            }
          } else {
            // Visible pages are paginated with maxSize
            startPage = ((Math.ceil(currentPage / maxSize) - 1) * maxSize) + 1;

            // Adjust last page if limit is exceeded
            endPage = Math.min(startPage + maxSize - 1, totalPages);
          }
        }

        // Add page number links
        for (var number = startPage; number <= endPage; number++) {
          var page = makePage(number, number, paginationCtrl.isActive(number), false);
          pages.push(page);
        }

        // Add links to move between page sets
        if ( isMaxSized && ! rotate ) {
          if ( startPage > 1 ) {
            var previousPageSet = makePage(startPage - 1, '...', false, false);
            pages.unshift(previousPageSet);
          }

          if ( endPage < totalPages ) {
            var nextPageSet = makePage(endPage + 1, '...', false, false);
            pages.push(nextPageSet);
          }
        }

        // Add previous & next links
        if (directionLinks) {
          var previousPage = makePage(currentPage - 1, previousText, false, paginationCtrl.noPrevious());
          pages.unshift(previousPage);

          var nextPage = makePage(currentPage + 1, nextText, false, paginationCtrl.noNext());
          pages.push(nextPage);
        }

        // Add first & last links
        if (boundaryLinks) {
          var firstPage = makePage(1, firstText, false, paginationCtrl.noPrevious());
          pages.unshift(firstPage);

          var lastPage = makePage(totalPages, lastText, false, paginationCtrl.noNext());
          pages.push(lastPage);
        }

        return pages;
      };
    }
  };
}])

.constant('pagerConfig', {
  itemsPerPage: 10,
  previousText: '« Previous',
  nextText: 'Next »',
  align: true
})

.directive('pager', ['pagerConfig', function(config) {
  return {
    restrict: 'EA',
    scope: {
      page: '=',
      totalItems: '=',
      onSelectPage:' &'
    },
    controller: 'PaginationController',
    templateUrl: 'template/pagination/pager.html',
    replace: true,
    link: function(scope, element, attrs, paginationCtrl) {

      // Setup configuration parameters
      var previousText = paginationCtrl.getAttributeValue(attrs.previousText, config.previousText, true),
      nextText         = paginationCtrl.getAttributeValue(attrs.nextText,     config.nextText,     true),
      align            = paginationCtrl.getAttributeValue(attrs.align,        config.align);

      paginationCtrl.init(config.itemsPerPage);

      // Create page object used in template
      function makePage(number, text, isDisabled, isPrevious, isNext) {
        return {
          number: number,
          text: text,
          disabled: isDisabled,
          previous: ( align && isPrevious ),
          next: ( align && isNext )
        };
      }

      paginationCtrl.getPages = function(currentPage) {
        return [
          makePage(currentPage - 1, previousText, paginationCtrl.noPrevious(), true, false),
          makePage(currentPage + 1, nextText, paginationCtrl.noNext(), false, true)
        ];
      };
    }
  };
}]);

/**
 * The following features are still outstanding: animation as a
 * function, placement as a function, inside, support for more triggers than
 * just mouse enter/leave, html tooltips, and selector delegation.
 */
angular.module( 'ui.bootstrap.tooltip', [ 'ui.bootstrap.position', 'ui.bootstrap.bindHtml' ] )

/**
 * The $tooltip service creates tooltip- and popover-like directives as well as
 * houses global options for them.
 */
.provider( '$tooltip', function () {
  // The default options tooltip and popover.
  var defaultOptions = {
    placement: 'top',
    animation: true,
    popupDelay: 0
  };

  // Default hide triggers for each show trigger
  var triggerMap = {
    'mouseenter': 'mouseleave',
    'click': 'click',
    'focus': 'blur'
  };

  // The options specified to the provider globally.
  var globalOptions = {};
  
  /**
   * `options({})` allows global configuration of all tooltips in the
   * application.
   *
   *   var app = angular.module( 'App', ['ui.bootstrap.tooltip'], function( $tooltipProvider ) {
   *     // place tooltips left instead of top by default
   *     $tooltipProvider.options( { placement: 'left' } );
   *   });
   */
	this.options = function( value ) {
		angular.extend( globalOptions, value );
	};

  /**
   * This allows you to extend the set of trigger mappings available. E.g.:
   *
   *   $tooltipProvider.setTriggers( 'openTrigger': 'closeTrigger' );
   */
  this.setTriggers = function setTriggers ( triggers ) {
    angular.extend( triggerMap, triggers );
  };

  /**
   * This is a helper function for translating camel-case to snake-case.
   */
  function snake_case(name){
    var regexp = /[A-Z]/g;
    var separator = '-';
    return name.replace(regexp, function(letter, pos) {
      return (pos ? separator : '') + letter.toLowerCase();
    });
  }

  /**
   * Returns the actual instance of the $tooltip service.
   * TODO support multiple triggers
   */
  this.$get = [ '$window', '$compile', '$timeout', '$parse', '$document', '$position', '$interpolate', function ( $window, $compile, $timeout, $parse, $document, $position, $interpolate ) {
    return function $tooltip ( type, prefix, defaultTriggerShow ) {
      var options = angular.extend( {}, defaultOptions, globalOptions );

      /**
       * Returns an object of show and hide triggers.
       *
       * If a trigger is supplied,
       * it is used to show the tooltip; otherwise, it will use the `trigger`
       * option passed to the `$tooltipProvider.options` method; else it will
       * default to the trigger supplied to this directive factory.
       *
       * The hide trigger is based on the show trigger. If the `trigger` option
       * was passed to the `$tooltipProvider.options` method, it will use the
       * mapped trigger from `triggerMap` or the passed trigger if the map is
       * undefined; otherwise, it uses the `triggerMap` value of the show
       * trigger; else it will just use the show trigger.
       */
      function getTriggers ( trigger ) {
        var show = trigger || options.trigger || defaultTriggerShow;
        var hide = triggerMap[show] || show;
        return {
          show: show,
          hide: hide
        };
      }

      var directiveName = snake_case( type );

      var startSym = $interpolate.startSymbol();
      var endSym = $interpolate.endSymbol();
      var template = 
        '<div '+ directiveName +'-popup '+
          'title="'+startSym+'tt_title'+endSym+'" '+
          'content="'+startSym+'tt_content'+endSym+'" '+
          'placement="'+startSym+'tt_placement'+endSym+'" '+
          'animation="tt_animation" '+
          'is-open="tt_isOpen"'+
          '>'+
        '</div>';

      return {
        restrict: 'EA',
        scope: true,
        link: function link ( scope, element, attrs ) {
          var tooltip = $compile( template )( scope );
          var transitionTimeout;
          var popupTimeout;
          var appendToBody = angular.isDefined( options.appendToBody ) ? options.appendToBody : false;
          var triggers = getTriggers( undefined );
          var hasRegisteredTriggers = false;
          var hasEnableExp = angular.isDefined(attrs[prefix+'Enable']);

          var positionTooltip = function (){
            var position,
              ttWidth,
              ttHeight,
              ttPosition;
            // Get the position of the directive element.
            position = appendToBody ? $position.offset( element ) : $position.position( element );

            // Get the height and width of the tooltip so we can center it.
            ttWidth = tooltip.prop( 'offsetWidth' );
            ttHeight = tooltip.prop( 'offsetHeight' );

            // Calculate the tooltip's top and left coordinates to center it with
            // this directive.
            switch ( scope.tt_placement ) {
              case 'right':
                ttPosition = {
                  top: position.top + position.height / 2 - ttHeight / 2,
                  left: position.left + position.width
                };
                break;
              case 'bottom':
                ttPosition = {
                  top: position.top + position.height,
                  left: position.left + position.width / 2 - ttWidth / 2
                };
                break;
              case 'left':
                ttPosition = {
                  top: position.top + position.height / 2 - ttHeight / 2,
                  left: position.left - ttWidth
                };
                break;
              default:
                ttPosition = {
                  top: position.top - ttHeight,
                  left: position.left + position.width / 2 - ttWidth / 2
                };
                break;
            }

            ttPosition.top += 'px';
            ttPosition.left += 'px';

            // Now set the calculated positioning.
            tooltip.css( ttPosition );

          };

          // By default, the tooltip is not open.
          // TODO add ability to start tooltip opened
          scope.tt_isOpen = false;

          function toggleTooltipBind () {
            if ( ! scope.tt_isOpen ) {
              showTooltipBind();
            } else {
              hideTooltipBind();
            }
          }
          
          // Show the tooltip with delay if specified, otherwise show it immediately
          function showTooltipBind() {
            if(hasEnableExp && !scope.$eval(attrs[prefix+'Enable'])) {
              return;
            }
            if ( scope.tt_popupDelay ) {
              popupTimeout = $timeout( show, scope.tt_popupDelay );
              popupTimeout.then(function(reposition){reposition();});
            } else {
              scope.$apply( show )();
            }
          }

          function hideTooltipBind () {
            scope.$apply(function () {
              hide();
            });
          }
          
          // Show the tooltip popup element.
          function show() {


            // Don't show empty tooltips.
            if ( ! scope.tt_content ) {
              return angular.noop;
            }

            // If there is a pending remove transition, we must cancel it, lest the
            // tooltip be mysteriously removed.
            if ( transitionTimeout ) {
              $timeout.cancel( transitionTimeout );
            }
            
            // Set the initial positioning.
            tooltip.css({ top: 0, left: 0, display: 'block' });
            
            // Now we add it to the DOM because need some info about it. But it's not 
            // visible yet anyway.
            if ( appendToBody ) {
                $document.find( 'body' ).append( tooltip );
            } else {
              element.after( tooltip );
            }

            positionTooltip();

            // And show the tooltip.
            scope.tt_isOpen = true;

            // Return positioning function as promise callback for correct
            // positioning after draw.
            return positionTooltip;
          }
          
          // Hide the tooltip popup element.
          function hide() {
            // First things first: we don't show it anymore.
            scope.tt_isOpen = false;

            //if tooltip is going to be shown after delay, we must cancel this
            $timeout.cancel( popupTimeout );
            
            // And now we remove it from the DOM. However, if we have animation, we 
            // need to wait for it to expire beforehand.
            // FIXME: this is a placeholder for a port of the transitions library.
            if ( scope.tt_animation ) {
              transitionTimeout = $timeout(function () {
                tooltip.remove();
              }, 500);
            } else {
              tooltip.remove();
            }
          }

          /**
           * Observe the relevant attributes.
           */
          attrs.$observe( type, function ( val ) {
            scope.tt_content = val;

            if (!val && scope.tt_isOpen ) {
              hide();
            }
          });

          attrs.$observe( prefix+'Title', function ( val ) {
            scope.tt_title = val;
          });

          attrs.$observe( prefix+'Placement', function ( val ) {
            scope.tt_placement = angular.isDefined( val ) ? val : options.placement;
          });

          attrs.$observe( prefix+'PopupDelay', function ( val ) {
            var delay = parseInt( val, 10 );
            scope.tt_popupDelay = ! isNaN(delay) ? delay : options.popupDelay;
          });

          var unregisterTriggers = function() {
            if (hasRegisteredTriggers) {
              element.unbind( triggers.show, showTooltipBind );
              element.unbind( triggers.hide, hideTooltipBind );
            }
          };

          attrs.$observe( prefix+'Trigger', function ( val ) {
            unregisterTriggers();

            triggers = getTriggers( val );

            if ( triggers.show === triggers.hide ) {
              element.bind( triggers.show, toggleTooltipBind );
            } else {
              element.bind( triggers.show, showTooltipBind );
              element.bind( triggers.hide, hideTooltipBind );
            }

            hasRegisteredTriggers = true;
          });

          var animation = scope.$eval(attrs[prefix + 'Animation']);
          scope.tt_animation = angular.isDefined(animation) ? !!animation : options.animation;

          attrs.$observe( prefix+'AppendToBody', function ( val ) {
            appendToBody = angular.isDefined( val ) ? $parse( val )( scope ) : appendToBody;
          });

          // if a tooltip is attached to <body> we need to remove it on
          // location change as its parent scope will probably not be destroyed
          // by the change.
          if ( appendToBody ) {
            scope.$on('$locationChangeSuccess', function closeTooltipOnLocationChangeSuccess () {
            if ( scope.tt_isOpen ) {
              hide();
            }
          });
          }

          // Make sure tooltip is destroyed and removed.
          scope.$on('$destroy', function onDestroyTooltip() {
            $timeout.cancel( transitionTimeout );
            $timeout.cancel( popupTimeout );
            unregisterTriggers();
            tooltip.remove();
            tooltip.unbind();
            tooltip = null;
          });
        }
      };
    };
  }];
})

.directive( 'tooltipPopup', function () {
  return {
    restrict: 'EA',
    replace: true,
    scope: { content: '@', placement: '@', animation: '&', isOpen: '&' },
    templateUrl: 'template/tooltip/tooltip-popup.html'
  };
})

.directive( 'tooltip', [ '$tooltip', function ( $tooltip ) {
  return $tooltip( 'tooltip', 'tooltip', 'mouseenter' );
}])

.directive( 'tooltipHtmlUnsafePopup', function () {
  return {
    restrict: 'EA',
    replace: true,
    scope: { content: '@', placement: '@', animation: '&', isOpen: '&' },
    templateUrl: 'template/tooltip/tooltip-html-unsafe-popup.html'
  };
})

.directive( 'tooltipHtmlUnsafe', [ '$tooltip', function ( $tooltip ) {
  return $tooltip( 'tooltipHtmlUnsafe', 'tooltip', 'mouseenter' );
}]);

/**
 * The following features are still outstanding: popup delay, animation as a
 * function, placement as a function, inside, support for more triggers than
 * just mouse enter/leave, html popovers, and selector delegatation.
 */
angular.module( 'ui.bootstrap.popover', [ 'ui.bootstrap.tooltip' ] )
.directive( 'popoverPopup', function () {
  return {
    restrict: 'EA',
    replace: true,
    scope: { title: '@', content: '@', placement: '@', animation: '&', isOpen: '&' },
    templateUrl: 'template/popover/popover.html'
  };
})
.directive( 'popover', [ '$compile', '$timeout', '$parse', '$window', '$tooltip', function ( $compile, $timeout, $parse, $window, $tooltip ) {
  return $tooltip( 'popover', 'popover', 'click' );
}]);


angular.module('ui.bootstrap.progressbar', ['ui.bootstrap.transition'])

.constant('progressConfig', {
  animate: true,
  max: 100
})

.controller('ProgressController', ['$scope', '$attrs', 'progressConfig', '$transition', function($scope, $attrs, progressConfig, $transition) {
    var self = this,
        bars = [],
        max = angular.isDefined($attrs.max) ? $scope.$parent.$eval($attrs.max) : progressConfig.max,
        animate = angular.isDefined($attrs.animate) ? $scope.$parent.$eval($attrs.animate) : progressConfig.animate;

    this.addBar = function(bar, element) {
        var oldValue = 0, index = bar.$parent.$index;
        if ( angular.isDefined(index) &&  bars[index] ) {
            oldValue = bars[index].value;
        }
        bars.push(bar);

        this.update(element, bar.value, oldValue);

        bar.$watch('value', function(value, oldValue) {
            if (value !== oldValue) {
                self.update(element, value, oldValue);
            }
        });

        bar.$on('$destroy', function() {
            self.removeBar(bar);
        });
    };

    // Update bar element width
    this.update = function(element, newValue, oldValue) {
        var percent = this.getPercentage(newValue);

        if (animate) {
            element.css('width', this.getPercentage(oldValue) + '%');
            $transition(element, {width: percent + '%'});
        } else {
            element.css({'transition': 'none', 'width': percent + '%'});
        }
    };

    this.removeBar = function(bar) {
        bars.splice(bars.indexOf(bar), 1);
    };

    this.getPercentage = function(value) {
        return Math.round(100 * value / max);
    };
}])

.directive('progress', function() {
    return {
        restrict: 'EA',
        replace: true,
        transclude: true,
        controller: 'ProgressController',
        require: 'progress',
        scope: {},
        template: '<div class="progress" ng-transclude></div>'
        //templateUrl: 'template/progressbar/progress.html' // Works in AngularJS 1.2
    };
})

.directive('bar', function() {
    return {
        restrict: 'EA',
        replace: true,
        transclude: true,
        require: '^progress',
        scope: {
            value: '=',
            type: '@'
        },
        templateUrl: 'template/progressbar/bar.html',
        link: function(scope, element, attrs, progressCtrl) {
            progressCtrl.addBar(scope, element);
        }
    };
})

.directive('progressbar', function() {
    return {
        restrict: 'EA',
        replace: true,
        transclude: true,
        controller: 'ProgressController',
        scope: {
            value: '=',
            type: '@'
        },
        templateUrl: 'template/progressbar/progressbar.html',
        link: function(scope, element, attrs, progressCtrl) {
            progressCtrl.addBar(scope, angular.element(element.children()[0]));
        }
    };
});
angular.module('ui.bootstrap.rating', [])

.constant('ratingConfig', {
  max: 5,
  stateOn: null,
  stateOff: null
})

.controller('RatingController', ['$scope', '$attrs', '$parse', 'ratingConfig', function($scope, $attrs, $parse, ratingConfig) {

  this.maxRange = angular.isDefined($attrs.max) ? $scope.$parent.$eval($attrs.max) : ratingConfig.max;
  this.stateOn = angular.isDefined($attrs.stateOn) ? $scope.$parent.$eval($attrs.stateOn) : ratingConfig.stateOn;
  this.stateOff = angular.isDefined($attrs.stateOff) ? $scope.$parent.$eval($attrs.stateOff) : ratingConfig.stateOff;

  this.createRateObjects = function(states) {
    var defaultOptions = {
      stateOn: this.stateOn,
      stateOff: this.stateOff
    };

    for (var i = 0, n = states.length; i < n; i++) {
      states[i] = angular.extend({ index: i }, defaultOptions, states[i]);
    }
    return states;
  };

  // Get objects used in template
  $scope.range = angular.isDefined($attrs.ratingStates) ?  this.createRateObjects(angular.copy($scope.$parent.$eval($attrs.ratingStates))): this.createRateObjects(new Array(this.maxRange));

  $scope.rate = function(value) {
    if ( $scope.readonly || $scope.value === value) {
      return;
    }

    $scope.value = value;
  };

  $scope.enter = function(value) {
    if ( ! $scope.readonly ) {
      $scope.val = value;
    }
    $scope.onHover({value: value});
  };

  $scope.reset = function() {
    $scope.val = angular.copy($scope.value);
    $scope.onLeave();
  };

  $scope.$watch('value', function(value) {
    $scope.val = value;
  });

  $scope.readonly = false;
  if ($attrs.readonly) {
    $scope.$parent.$watch($parse($attrs.readonly), function(value) {
      $scope.readonly = !!value;
    });
  }
}])

.directive('rating', function() {
  return {
    restrict: 'EA',
    scope: {
      value: '=',
      onHover: '&',
      onLeave: '&'
    },
    controller: 'RatingController',
    templateUrl: 'template/rating/rating.html',
    replace: true
  };
});

/**
 * @ngdoc overview
 * @name ui.bootstrap.tabs
 *
 * @description
 * AngularJS version of the tabs directive.
 */

angular.module('ui.bootstrap.tabs', [])

.controller('TabsetController', ['$scope', function TabsetCtrl($scope) {
  var ctrl = this,
      tabs = ctrl.tabs = $scope.tabs = [];

  ctrl.select = function(tab) {
    angular.forEach(tabs, function(tab) {
      tab.active = false;
    });
    tab.active = true;
  };

  ctrl.addTab = function addTab(tab) {
    tabs.push(tab);
    if (tabs.length === 1 || tab.active) {
      ctrl.select(tab);
    }
  };

  ctrl.removeTab = function removeTab(tab) {
    var index = tabs.indexOf(tab);
    //Select a new tab if the tab to be removed is selected
    if (tab.active && tabs.length > 1) {
      //If this is the last tab, select the previous tab. else, the next tab.
      var newActiveIndex = index == tabs.length - 1 ? index - 1 : index + 1;
      ctrl.select(tabs[newActiveIndex]);
    }
    tabs.splice(index, 1);
  };
}])

/**
 * @ngdoc directive
 * @name ui.bootstrap.tabs.directive:tabset
 * @restrict EA
 *
 * @description
 * Tabset is the outer container for the tabs directive
 *
 * @param {boolean=} vertical Whether or not to use vertical styling for the tabs.
 * @param {boolean=} justified Whether or not to use justified styling for the tabs.
 *
 * @example
<example module="ui.bootstrap">
  <file name="index.html">
    <tabset>
      <tab heading="Tab 1"><b>First</b> Content!</tab>
      <tab heading="Tab 2"><i>Second</i> Content!</tab>
    </tabset>
    <hr />
    <tabset vertical="true">
      <tab heading="Vertical Tab 1"><b>First</b> Vertical Content!</tab>
      <tab heading="Vertical Tab 2"><i>Second</i> Vertical Content!</tab>
    </tabset>
    <tabset justified="true">
      <tab heading="Justified Tab 1"><b>First</b> Justified Content!</tab>
      <tab heading="Justified Tab 2"><i>Second</i> Justified Content!</tab>
    </tabset>
  </file>
</example>
 */
.directive('tabset', function() {
  return {
    restrict: 'EA',
    transclude: true,
    replace: true,
    scope: {},
    controller: 'TabsetController',
    templateUrl: 'template/tabs/tabset.html',
    link: function(scope, element, attrs) {
      scope.vertical = angular.isDefined(attrs.vertical) ? scope.$parent.$eval(attrs.vertical) : false;
      scope.justified = angular.isDefined(attrs.justified) ? scope.$parent.$eval(attrs.justified) : false;
      scope.type = angular.isDefined(attrs.type) ? scope.$parent.$eval(attrs.type) : 'tabs';
    }
  };
})

/**
 * @ngdoc directive
 * @name ui.bootstrap.tabs.directive:tab
 * @restrict EA
 *
 * @param {string=} heading The visible heading, or title, of the tab. Set HTML headings with {@link ui.bootstrap.tabs.directive:tabHeading tabHeading}.
 * @param {string=} select An expression to evaluate when the tab is selected.
 * @param {boolean=} active A binding, telling whether or not this tab is selected.
 * @param {boolean=} disabled A binding, telling whether or not this tab is disabled.
 *
 * @description
 * Creates a tab with a heading and content. Must be placed within a {@link ui.bootstrap.tabs.directive:tabset tabset}.
 *
 * @example
<example module="ui.bootstrap">
  <file name="index.html">
    <div ng-controller="TabsDemoCtrl">
      <button class="btn btn-small" ng-click="items[0].active = true">
        Select item 1, using active binding
      </button>
      <button class="btn btn-small" ng-click="items[1].disabled = !items[1].disabled">
        Enable/disable item 2, using disabled binding
      </button>
      <br />
      <tabset>
        <tab heading="Tab 1">First Tab</tab>
        <tab select="alertMe()">
          <tab-heading><i class="icon-bell"></i> Alert me!</tab-heading>
          Second Tab, with alert callback and html heading!
        </tab>
        <tab ng-repeat="item in items"
          heading="{{item.title}}"
          disabled="item.disabled"
          active="item.active">
          {{item.content}}
        </tab>
      </tabset>
    </div>
  </file>
  <file name="script.js">
    function TabsDemoCtrl($scope) {
      $scope.items = [
        { title:"Dynamic Title 1", content:"Dynamic Item 0" },
        { title:"Dynamic Title 2", content:"Dynamic Item 1", disabled: true }
      ];

      $scope.alertMe = function() {
        setTimeout(function() {
          alert("You've selected the alert tab!");
        });
      };
    };
  </file>
</example>
 */

/**
 * @ngdoc directive
 * @name ui.bootstrap.tabs.directive:tabHeading
 * @restrict EA
 *
 * @description
 * Creates an HTML heading for a {@link ui.bootstrap.tabs.directive:tab tab}. Must be placed as a child of a tab element.
 *
 * @example
<example module="ui.bootstrap">
  <file name="index.html">
    <tabset>
      <tab>
        <tab-heading><b>HTML</b> in my titles?!</tab-heading>
        And some content, too!
      </tab>
      <tab>
        <tab-heading><i class="icon-heart"></i> Icon heading?!?</tab-heading>
        That's right.
      </tab>
    </tabset>
  </file>
</example>
 */
.directive('tab', ['$parse', function($parse) {
  return {
    require: '^tabset',
    restrict: 'EA',
    replace: true,
    templateUrl: 'template/tabs/tab.html',
    transclude: true,
    scope: {
      heading: '@',
      onSelect: '&select', //This callback is called in contentHeadingTransclude
                          //once it inserts the tab's content into the dom
      onDeselect: '&deselect'
    },
    controller: function() {
      //Empty controller so other directives can require being 'under' a tab
    },
    compile: function(elm, attrs, transclude) {
      return function postLink(scope, elm, attrs, tabsetCtrl) {
        var getActive, setActive;
        if (attrs.active) {
          getActive = $parse(attrs.active);
          setActive = getActive.assign;
          scope.$parent.$watch(getActive, function updateActive(value, oldVal) {
            // Avoid re-initializing scope.active as it is already initialized
            // below. (watcher is called async during init with value ===
            // oldVal)
            if (value !== oldVal) {
              scope.active = !!value;
            }
          });
          scope.active = getActive(scope.$parent);
        } else {
          setActive = getActive = angular.noop;
        }

        scope.$watch('active', function(active) {
          // Note this watcher also initializes and assigns scope.active to the
          // attrs.active expression.
          setActive(scope.$parent, active);
          if (active) {
            tabsetCtrl.select(scope);
            scope.onSelect();
          } else {
            scope.onDeselect();
          }
        });

        scope.disabled = false;
        if ( attrs.disabled ) {
          scope.$parent.$watch($parse(attrs.disabled), function(value) {
            scope.disabled = !! value;
          });
        }

        scope.select = function() {
          if ( ! scope.disabled ) {
            scope.active = true;
          }
        };

        tabsetCtrl.addTab(scope);
        scope.$on('$destroy', function() {
          tabsetCtrl.removeTab(scope);
        });


        //We need to transclude later, once the content container is ready.
        //when this link happens, we're inside a tab heading.
        scope.$transcludeFn = transclude;
      };
    }
  };
}])

.directive('tabHeadingTransclude', [function() {
  return {
    restrict: 'A',
    require: '^tab',
    link: function(scope, elm, attrs, tabCtrl) {
      scope.$watch('headingElement', function updateHeadingElement(heading) {
        if (heading) {
          elm.html('');
          elm.append(heading);
        }
      });
    }
  };
}])

.directive('tabContentTransclude', function() {
  return {
    restrict: 'A',
    require: '^tabset',
    link: function(scope, elm, attrs) {
      var tab = scope.$eval(attrs.tabContentTransclude);

      //Now our tab is ready to be transcluded: both the tab heading area
      //and the tab content area are loaded.  Transclude 'em both.
      tab.$transcludeFn(tab.$parent, function(contents) {
        angular.forEach(contents, function(node) {
          if (isTabHeading(node)) {
            //Let tabHeadingTransclude know.
            tab.headingElement = node;
          } else {
            elm.append(node);
          }
        });
      });
    }
  };
  function isTabHeading(node) {
    return node.tagName &&  (
      node.hasAttribute('tab-heading') ||
      node.hasAttribute('data-tab-heading') ||
      node.tagName.toLowerCase() === 'tab-heading' ||
      node.tagName.toLowerCase() === 'data-tab-heading'
    );
  }
})

;

angular.module('ui.bootstrap.timepicker', [])

.constant('timepickerConfig', {
  hourStep: 1,
  minuteStep: 1,
  showMeridian: true,
  meridians: null,
  readonlyInput: false,
  mousewheel: true
})

.directive('timepicker', ['$parse', '$log', 'timepickerConfig', '$locale', function ($parse, $log, timepickerConfig, $locale) {
  return {
    restrict: 'EA',
    require:'?^ngModel',
    replace: true,
    scope: {},
    templateUrl: 'template/timepicker/timepicker.html',
    link: function(scope, element, attrs, ngModel) {
      if ( !ngModel ) {
        return; // do nothing if no ng-model
      }

      var selected = new Date(),
          meridians = angular.isDefined(attrs.meridians) ? scope.$parent.$eval(attrs.meridians) : timepickerConfig.meridians || $locale.DATETIME_FORMATS.AMPMS;

      var hourStep = timepickerConfig.hourStep;
      if (attrs.hourStep) {
        scope.$parent.$watch($parse(attrs.hourStep), function(value) {
          hourStep = parseInt(value, 10);
        });
      }

      var minuteStep = timepickerConfig.minuteStep;
      if (attrs.minuteStep) {
        scope.$parent.$watch($parse(attrs.minuteStep), function(value) {
          minuteStep = parseInt(value, 10);
        });
      }

      // 12H / 24H mode
      scope.showMeridian = timepickerConfig.showMeridian;
      if (attrs.showMeridian) {
        scope.$parent.$watch($parse(attrs.showMeridian), function(value) {
          scope.showMeridian = !!value;

          if ( ngModel.$error.time ) {
            // Evaluate from template
            var hours = getHoursFromTemplate(), minutes = getMinutesFromTemplate();
            if (angular.isDefined( hours ) && angular.isDefined( minutes )) {
              selected.setHours( hours );
              refresh();
            }
          } else {
            updateTemplate();
          }
        });
      }

      // Get scope.hours in 24H mode if valid
      function getHoursFromTemplate ( ) {
        var hours = parseInt( scope.hours, 10 );
        var valid = ( scope.showMeridian ) ? (hours > 0 && hours < 13) : (hours >= 0 && hours < 24);
        if ( !valid ) {
          return undefined;
        }

        if ( scope.showMeridian ) {
          if ( hours === 12 ) {
            hours = 0;
          }
          if ( scope.meridian === meridians[1] ) {
            hours = hours + 12;
          }
        }
        return hours;
      }

      function getMinutesFromTemplate() {
        var minutes = parseInt(scope.minutes, 10);
        return ( minutes >= 0 && minutes < 60 ) ? minutes : undefined;
      }

      function pad( value ) {
        return ( angular.isDefined(value) && value.toString().length < 2 ) ? '0' + value : value;
      }

      // Input elements
      var inputs = element.find('input'), hoursInputEl = inputs.eq(0), minutesInputEl = inputs.eq(1);

      // Respond on mousewheel spin
      var mousewheel = (angular.isDefined(attrs.mousewheel)) ? scope.$eval(attrs.mousewheel) : timepickerConfig.mousewheel;
      if ( mousewheel ) {

        var isScrollingUp = function(e) {
          if (e.originalEvent) {
            e = e.originalEvent;
          }
          //pick correct delta variable depending on event
          var delta = (e.wheelDelta) ? e.wheelDelta : -e.deltaY;
          return (e.detail || delta > 0);
        };

        hoursInputEl.bind('mousewheel wheel', function(e) {
          scope.$apply( (isScrollingUp(e)) ? scope.incrementHours() : scope.decrementHours() );
          e.preventDefault();
        });

        minutesInputEl.bind('mousewheel wheel', function(e) {
          scope.$apply( (isScrollingUp(e)) ? scope.incrementMinutes() : scope.decrementMinutes() );
          e.preventDefault();
        });
      }

      scope.readonlyInput = (angular.isDefined(attrs.readonlyInput)) ? scope.$eval(attrs.readonlyInput) : timepickerConfig.readonlyInput;
      if ( ! scope.readonlyInput ) {

        var invalidate = function(invalidHours, invalidMinutes) {
          ngModel.$setViewValue( null );
          ngModel.$setValidity('time', false);
          if (angular.isDefined(invalidHours)) {
            scope.invalidHours = invalidHours;
          }
          if (angular.isDefined(invalidMinutes)) {
            scope.invalidMinutes = invalidMinutes;
          }
        };

        scope.updateHours = function() {
          var hours = getHoursFromTemplate();

          if ( angular.isDefined(hours) ) {
            selected.setHours( hours );
            refresh( 'h' );
          } else {
            invalidate(true);
          }
        };

        hoursInputEl.bind('blur', function(e) {
          if ( !scope.validHours && scope.hours < 10) {
            scope.$apply( function() {
              scope.hours = pad( scope.hours );
            });
          }
        });

        scope.updateMinutes = function() {
          var minutes = getMinutesFromTemplate();

          if ( angular.isDefined(minutes) ) {
            selected.setMinutes( minutes );
            refresh( 'm' );
          } else {
            invalidate(undefined, true);
          }
        };

        minutesInputEl.bind('blur', function(e) {
          if ( !scope.invalidMinutes && scope.minutes < 10 ) {
            scope.$apply( function() {
              scope.minutes = pad( scope.minutes );
            });
          }
        });
      } else {
        scope.updateHours = angular.noop;
        scope.updateMinutes = angular.noop;
      }

      ngModel.$render = function() {
        var date = ngModel.$modelValue ? new Date( ngModel.$modelValue ) : null;

        if ( isNaN(date) ) {
          ngModel.$setValidity('time', false);
          $log.error('Timepicker directive: "ng-model" value must be a Date object, a number of milliseconds since 01.01.1970 or a string representing an RFC2822 or ISO 8601 date.');
        } else {
          if ( date ) {
            selected = date;
          }
          makeValid();
          updateTemplate();
        }
      };

      // Call internally when we know that model is valid.
      function refresh( keyboardChange ) {
        makeValid();
        ngModel.$setViewValue( new Date(selected) );
        updateTemplate( keyboardChange );
      }

      function makeValid() {
        ngModel.$setValidity('time', true);
        scope.invalidHours = false;
        scope.invalidMinutes = false;
      }

      function updateTemplate( keyboardChange ) {
        var hours = selected.getHours(), minutes = selected.getMinutes();

        if ( scope.showMeridian ) {
          hours = ( hours === 0 || hours === 12 ) ? 12 : hours % 12; // Convert 24 to 12 hour system
        }
        scope.hours =  keyboardChange === 'h' ? hours : pad(hours);
        scope.minutes = keyboardChange === 'm' ? minutes : pad(minutes);
        scope.meridian = selected.getHours() < 12 ? meridians[0] : meridians[1];
      }

      function addMinutes( minutes ) {
        var dt = new Date( selected.getTime() + minutes * 60000 );
        selected.setHours( dt.getHours(), dt.getMinutes() );
        refresh();
      }

      scope.incrementHours = function() {
        addMinutes( hourStep * 60 );
      };
      scope.decrementHours = function() {
        addMinutes( - hourStep * 60 );
      };
      scope.incrementMinutes = function() {
        addMinutes( minuteStep );
      };
      scope.decrementMinutes = function() {
        addMinutes( - minuteStep );
      };
      scope.toggleMeridian = function() {
        addMinutes( 12 * 60 * (( selected.getHours() < 12 ) ? 1 : -1) );
      };
    }
  };
}]);

angular.module('ui.bootstrap.typeahead', ['ui.bootstrap.position', 'ui.bootstrap.bindHtml'])

/**
 * A helper service that can parse typeahead's syntax (string provided by users)
 * Extracted to a separate service for ease of unit testing
 */
  .factory('typeaheadParser', ['$parse', function ($parse) {

  //                      00000111000000000000022200000000000000003333333333333330000000000044000
  var TYPEAHEAD_REGEXP = /^\s*(.*?)(?:\s+as\s+(.*?))?\s+for\s+(?:([\$\w][\$\w\d]*))\s+in\s+(.*)$/;

  return {
    parse:function (input) {

      var match = input.match(TYPEAHEAD_REGEXP), modelMapper, viewMapper, source;
      if (!match) {
        throw new Error(
          "Expected typeahead specification in form of '_modelValue_ (as _label_)? for _item_ in _collection_'" +
            " but got '" + input + "'.");
      }

      return {
        itemName:match[3],
        source:$parse(match[4]),
        viewMapper:$parse(match[2] || match[1]),
        modelMapper:$parse(match[1])
      };
    }
  };
}])

  .directive('typeahead', ['$compile', '$parse', '$q', '$timeout', '$document', '$position', 'typeaheadParser',
    function ($compile, $parse, $q, $timeout, $document, $position, typeaheadParser) {

  var HOT_KEYS = [9, 13, 27, 38, 40];

  return {
    require:'ngModel',
    link:function (originalScope, element, attrs, modelCtrl) {

      //SUPPORTED ATTRIBUTES (OPTIONS)

      //minimal no of characters that needs to be entered before typeahead kicks-in
      var minSearch = originalScope.$eval(attrs.typeaheadMinLength) || 1;

      //minimal wait time after last character typed before typehead kicks-in
      var waitTime = originalScope.$eval(attrs.typeaheadWaitMs) || 0;

      //should it restrict model values to the ones selected from the popup only?
      var isEditable = originalScope.$eval(attrs.typeaheadEditable) !== false;

      //binding to a variable that indicates if matches are being retrieved asynchronously
      var isLoadingSetter = $parse(attrs.typeaheadLoading).assign || angular.noop;

      //a callback executed when a match is selected
      var onSelectCallback = $parse(attrs.typeaheadOnSelect);

      var inputFormatter = attrs.typeaheadInputFormatter ? $parse(attrs.typeaheadInputFormatter) : undefined;

      var appendToBody =  attrs.typeaheadAppendToBody ? $parse(attrs.typeaheadAppendToBody) : false;

      //INTERNAL VARIABLES

      //model setter executed upon match selection
      var $setModelValue = $parse(attrs.ngModel).assign;

      //expressions used by typeahead
      var parserResult = typeaheadParser.parse(attrs.typeahead);

      var hasFocus;

      //pop-up element used to display matches
      var popUpEl = angular.element('<div typeahead-popup></div>');
      popUpEl.attr({
        matches: 'matches',
        active: 'activeIdx',
        select: 'select(activeIdx)',
        query: 'query',
        position: 'position'
      });
      //custom item template
      if (angular.isDefined(attrs.typeaheadTemplateUrl)) {
        popUpEl.attr('template-url', attrs.typeaheadTemplateUrl);
      }

      //create a child scope for the typeahead directive so we are not polluting original scope
      //with typeahead-specific data (matches, query etc.)
      var scope = originalScope.$new();
      originalScope.$on('$destroy', function(){
        scope.$destroy();
      });

      var resetMatches = function() {
        scope.matches = [];
        scope.activeIdx = -1;
      };

      var getMatchesAsync = function(inputValue) {

        var locals = {$viewValue: inputValue};
        isLoadingSetter(originalScope, true);
        $q.when(parserResult.source(originalScope, locals)).then(function(matches) {

          //it might happen that several async queries were in progress if a user were typing fast
          //but we are interested only in responses that correspond to the current view value
          if (inputValue === modelCtrl.$viewValue && hasFocus) {
            if (matches.length > 0) {

              scope.activeIdx = 0;
              scope.matches.length = 0;

              //transform labels
              for(var i=0; i<matches.length; i++) {
                locals[parserResult.itemName] = matches[i];
                scope.matches.push({
                  label: parserResult.viewMapper(scope, locals),
                  model: matches[i]
                });
              }

              scope.query = inputValue;
              //position pop-up with matches - we need to re-calculate its position each time we are opening a window
              //with matches as a pop-up might be absolute-positioned and position of an input might have changed on a page
              //due to other elements being rendered
              scope.position = appendToBody ? $position.offset(element) : $position.position(element);
              scope.position.top = scope.position.top + element.prop('offsetHeight');

            } else {
              resetMatches();
            }
            isLoadingSetter(originalScope, false);
          }
        }, function(){
          resetMatches();
          isLoadingSetter(originalScope, false);
        });
      };

      resetMatches();

      //we need to propagate user's query so we can higlight matches
      scope.query = undefined;

      //Declare the timeout promise var outside the function scope so that stacked calls can be cancelled later 
      var timeoutPromise;

      //plug into $parsers pipeline to open a typeahead on view changes initiated from DOM
      //$parsers kick-in on all the changes coming from the view as well as manually triggered by $setViewValue
      modelCtrl.$parsers.unshift(function (inputValue) {

        hasFocus = true;

        if (inputValue && inputValue.length >= minSearch) {
          if (waitTime > 0) {
            if (timeoutPromise) {
              $timeout.cancel(timeoutPromise);//cancel previous timeout
            }
            timeoutPromise = $timeout(function () {
              getMatchesAsync(inputValue);
            }, waitTime);
          } else {
            getMatchesAsync(inputValue);
          }
        } else {
          isLoadingSetter(originalScope, false);
          resetMatches();
        }

        if (isEditable) {
          return inputValue;
        } else {
          if (!inputValue) {
            // Reset in case user had typed something previously.
            modelCtrl.$setValidity('editable', true);
            return inputValue;
          } else {
            modelCtrl.$setValidity('editable', false);
            return undefined;
          }
        }
      });

      modelCtrl.$formatters.push(function (modelValue) {

        var candidateViewValue, emptyViewValue;
        var locals = {};

        if (inputFormatter) {

          locals['$model'] = modelValue;
          return inputFormatter(originalScope, locals);

        } else {

          //it might happen that we don't have enough info to properly render input value
          //we need to check for this situation and simply return model value if we can't apply custom formatting
          locals[parserResult.itemName] = modelValue;
          candidateViewValue = parserResult.viewMapper(originalScope, locals);
          locals[parserResult.itemName] = undefined;
          emptyViewValue = parserResult.viewMapper(originalScope, locals);

          return candidateViewValue!== emptyViewValue ? candidateViewValue : modelValue;
        }
      });

      scope.select = function (activeIdx) {
        //called from within the $digest() cycle
        var locals = {};
        var model, item;

        locals[parserResult.itemName] = item = scope.matches[activeIdx].model;
        model = parserResult.modelMapper(originalScope, locals);
        $setModelValue(originalScope, model);
        modelCtrl.$setValidity('editable', true);

        onSelectCallback(originalScope, {
          $item: item,
          $model: model,
          $label: parserResult.viewMapper(originalScope, locals)
        });

        resetMatches();

        //return focus to the input element if a mach was selected via a mouse click event
        element[0].focus();
      };

      //bind keyboard events: arrows up(38) / down(40), enter(13) and tab(9), esc(27)
      element.bind('keydown', function (evt) {

        //typeahead is open and an "interesting" key was pressed
        if (scope.matches.length === 0 || HOT_KEYS.indexOf(evt.which) === -1) {
          return;
        }

        evt.preventDefault();

        if (evt.which === 40) {
          scope.activeIdx = (scope.activeIdx + 1) % scope.matches.length;
          scope.$digest();

        } else if (evt.which === 38) {
          scope.activeIdx = (scope.activeIdx ? scope.activeIdx : scope.matches.length) - 1;
          scope.$digest();

        } else if (evt.which === 13 || evt.which === 9) {
          scope.$apply(function () {
            scope.select(scope.activeIdx);
          });

        } else if (evt.which === 27) {
          evt.stopPropagation();

          resetMatches();
          scope.$digest();
        }
      });

      element.bind('blur', function (evt) {
        hasFocus = false;
      });

      // Keep reference to click handler to unbind it.
      var dismissClickHandler = function (evt) {
        if (element[0] !== evt.target) {
          resetMatches();
          scope.$digest();
        }
      };

      $document.bind('click', dismissClickHandler);

      originalScope.$on('$destroy', function(){
        $document.unbind('click', dismissClickHandler);
      });

      var $popup = $compile(popUpEl)(scope);
      if ( appendToBody ) {
        $document.find('body').append($popup);
      } else {
        element.after($popup);
      }
    }
  };

}])

  .directive('typeaheadPopup', function () {
    return {
      restrict:'EA',
      scope:{
        matches:'=',
        query:'=',
        active:'=',
        position:'=',
        select:'&'
      },
      replace:true,
      templateUrl:'template/typeahead/typeahead-popup.html',
      link:function (scope, element, attrs) {

        scope.templateUrl = attrs.templateUrl;

        scope.isOpen = function () {
          return scope.matches.length > 0;
        };

        scope.isActive = function (matchIdx) {
          return scope.active == matchIdx;
        };

        scope.selectActive = function (matchIdx) {
          scope.active = matchIdx;
        };

        scope.selectMatch = function (activeIdx) {
          scope.select({activeIdx:activeIdx});
        };
      }
    };
  })

  .directive('typeaheadMatch', ['$http', '$templateCache', '$compile', '$parse', function ($http, $templateCache, $compile, $parse) {
    return {
      restrict:'EA',
      scope:{
        index:'=',
        match:'=',
        query:'='
      },
      link:function (scope, element, attrs) {
        var tplUrl = $parse(attrs.templateUrl)(scope.$parent) || 'template/typeahead/typeahead-match.html';
        $http.get(tplUrl, {cache: $templateCache}).success(function(tplContent){
           element.replaceWith($compile(tplContent.trim())(scope));
        });
      }
    };
  }])

  .filter('typeaheadHighlight', function() {

    function escapeRegexp(queryToEscape) {
      return queryToEscape.replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
    }

    return function(matchItem, query) {
      return query ? matchItem.replace(new RegExp(escapeRegexp(query), 'gi'), '<strong>$&</strong>') : matchItem;
    };
  });
angular.module("template/accordion/accordion-group.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/accordion/accordion-group.html",
    "<div class=\"panel panel-default\">\n" +
    "  <div class=\"panel-heading\">\n" +
    "    <h4 class=\"panel-title\">\n" +
    "      <a class=\"accordion-toggle\" ng-click=\"isOpen = !isOpen\" accordion-transclude=\"heading\">{{heading}}</a>\n" +
    "    </h4>\n" +
    "  </div>\n" +
    "  <div class=\"panel-collapse\" collapse=\"!isOpen\">\n" +
    "	  <div class=\"panel-body\" ng-transclude></div>\n" +
    "  </div>\n" +
    "</div>");
}]);

angular.module("template/accordion/accordion.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/accordion/accordion.html",
    "<div class=\"panel-group\" ng-transclude></div>");
}]);

angular.module("template/alert/alert.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/alert/alert.html",
    "<div class='alert' ng-class='\"alert-\" + (type || \"warning\")'>\n" +
    "    <button ng-show='closeable' type='button' class='close' ng-click='close()'>&times;</button>\n" +
    "    <div ng-transclude></div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("template/carousel/carousel.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/carousel/carousel.html",
    "<div ng-mouseenter=\"pause()\" ng-mouseleave=\"play()\" class=\"carousel\">\n" +
    "    <ol class=\"carousel-indicators\" ng-show=\"slides().length > 1\">\n" +
    "        <li ng-repeat=\"slide in slides()\" ng-class=\"{active: isActive(slide)}\" ng-click=\"select(slide)\"></li>\n" +
    "    </ol>\n" +
    "    <div class=\"carousel-inner\" ng-transclude></div>\n" +
    "    <a class=\"left carousel-control\" ng-click=\"prev()\" ng-show=\"slides().length > 1\"><span class=\"icon-prev\"></span></a>\n" +
    "    <a class=\"right carousel-control\" ng-click=\"next()\" ng-show=\"slides().length > 1\"><span class=\"icon-next\"></span></a>\n" +
    "</div>\n" +
    "");
}]);

angular.module("template/carousel/slide.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/carousel/slide.html",
    "<div ng-class=\"{\n" +
    "    'active': leaving || (active && !entering),\n" +
    "    'prev': (next || active) && direction=='prev',\n" +
    "    'next': (next || active) && direction=='next',\n" +
    "    'right': direction=='prev',\n" +
    "    'left': direction=='next'\n" +
    "  }\" class=\"item text-center\" ng-transclude></div>\n" +
    "");
}]);

angular.module("template/datepicker/datepicker.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/datepicker/datepicker.html",
    "<table>\n" +
    "  <thead>\n" +
    "    <tr>\n" +
    "      <th><button type=\"button\" class=\"btn btn-default btn-sm pull-left\" ng-click=\"move(-1)\"><i class=\"glyphicon glyphicon-chevron-left\"></i></button></th>\n" +
    "      <th colspan=\"{{rows[0].length - 2 + showWeekNumbers}}\"><button type=\"button\" class=\"btn btn-default btn-sm btn-block\" ng-click=\"toggleMode()\"><strong>{{title}}</strong></button></th>\n" +
    "      <th><button type=\"button\" class=\"btn btn-default btn-sm pull-right\" ng-click=\"move(1)\"><i class=\"glyphicon glyphicon-chevron-right\"></i></button></th>\n" +
    "    </tr>\n" +
    "    <tr ng-show=\"labels.length > 0\" class=\"h6\">\n" +
    "      <th ng-show=\"showWeekNumbers\" class=\"text-center\">#</th>\n" +
    "      <th ng-repeat=\"label in labels\" class=\"text-center\">{{label}}</th>\n" +
    "    </tr>\n" +
    "  </thead>\n" +
    "  <tbody>\n" +
    "    <tr ng-repeat=\"row in rows\">\n" +
    "      <td ng-show=\"showWeekNumbers\" class=\"text-center\"><em>{{ getWeekNumber(row) }}</em></td>\n" +
    "      <td ng-repeat=\"dt in row\" class=\"text-center\">\n" +
    "        <button type=\"button\" style=\"width:100%;\" class=\"btn btn-default btn-sm\" ng-class=\"{'btn-info': dt.selected}\" ng-click=\"select(dt.date)\" ng-disabled=\"dt.disabled\"><span ng-class=\"{'text-muted': dt.secondary}\">{{dt.label}}</span></button>\n" +
    "      </td>\n" +
    "    </tr>\n" +
    "  </tbody>\n" +
    "</table>\n" +
    "");
}]);

angular.module("template/datepicker/popup.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/datepicker/popup.html",
    "<ul class=\"dropdown-menu\" ng-style=\"{display: (isOpen && 'block') || 'none', top: position.top+'px', left: position.left+'px'}\">\n" +
    "	<li ng-transclude></li>\n" +
    "	<li ng-show=\"showButtonBar\" style=\"padding:10px 9px 2px\">\n" +
    "		<span class=\"btn-group\">\n" +
    "			<button type=\"button\" class=\"btn btn-sm btn-info\" ng-click=\"today()\">{{currentText}}</button>\n" +
    "			<button type=\"button\" class=\"btn btn-sm btn-default\" ng-click=\"showWeeks = ! showWeeks\" ng-class=\"{active: showWeeks}\">{{toggleWeeksText}}</button>\n" +
    "			<button type=\"button\" class=\"btn btn-sm btn-danger\" ng-click=\"clear()\">{{clearText}}</button>\n" +
    "		</span>\n" +
    "		<button type=\"button\" class=\"btn btn-sm btn-success pull-right\" ng-click=\"isOpen = false\">{{closeText}}</button>\n" +
    "	</li>\n" +
    "</ul>\n" +
    "");
}]);

angular.module("template/modal/backdrop.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/modal/backdrop.html",
    "<div class=\"modal-backdrop fade\" ng-class=\"{in: animate}\" ng-style=\"{'z-index': 1040 + index*10}\"></div>");
}]);

angular.module("template/modal/window.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/modal/window.html",
    "<div tabindex=\"-1\" class=\"modal fade {{ windowClass }}\" ng-class=\"{in: animate}\" ng-style=\"{'z-index': 1050 + index*10, display: 'block'}\" ng-click=\"close($event)\">\n" +
    "    <div class=\"modal-dialog\"><div class=\"modal-content\" ng-transclude></div></div>\n" +
    "</div>");
}]);

angular.module("template/pagination/pager.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/pagination/pager.html",
    "<ul class=\"pager\">\n" +
    "  <li ng-repeat=\"page in pages\" ng-class=\"{disabled: page.disabled, previous: page.previous, next: page.next}\"><a ng-click=\"selectPage(page.number)\">{{page.text}}</a></li>\n" +
    "</ul>");
}]);

angular.module("template/pagination/pagination.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/pagination/pagination.html",
    "<ul class=\"pagination\">\n" +
    "  <li ng-repeat=\"page in pages\" ng-class=\"{active: page.active, disabled: page.disabled}\"><a ng-click=\"selectPage(page.number)\">{{page.text}}</a></li>\n" +
    "</ul>");
}]);

angular.module("template/tooltip/tooltip-html-unsafe-popup.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/tooltip/tooltip-html-unsafe-popup.html",
    "<div class=\"tooltip {{placement}}\" ng-class=\"{ in: isOpen(), fade: animation() }\">\n" +
    "  <div class=\"tooltip-arrow\"></div>\n" +
    "  <div class=\"tooltip-inner\" bind-html-unsafe=\"content\"></div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("template/tooltip/tooltip-popup.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/tooltip/tooltip-popup.html",
    "<div class=\"tooltip {{placement}}\" ng-class=\"{ in: isOpen(), fade: animation() }\">\n" +
    "  <div class=\"tooltip-arrow\"></div>\n" +
    "  <div class=\"tooltip-inner\" ng-bind=\"content\"></div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("template/popover/popover.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/popover/popover.html",
    "<div class=\"popover {{placement}}\" ng-class=\"{ in: isOpen(), fade: animation() }\">\n" +
    "  <div class=\"arrow\"></div>\n" +
    "\n" +
    "  <div class=\"popover-inner\">\n" +
    "      <h3 class=\"popover-title\" ng-bind=\"title\" ng-show=\"title\"></h3>\n" +
    "      <div class=\"popover-content\" ng-bind=\"content\"></div>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("template/progressbar/bar.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/progressbar/bar.html",
    "<div class=\"progress-bar\" ng-class=\"type && 'progress-bar-' + type\" ng-transclude></div>");
}]);

angular.module("template/progressbar/progress.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/progressbar/progress.html",
    "<div class=\"progress\" ng-transclude></div>");
}]);

angular.module("template/progressbar/progressbar.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/progressbar/progressbar.html",
    "<div class=\"progress\"><div class=\"progress-bar\" ng-class=\"type && 'progress-bar-' + type\" ng-transclude></div></div>");
}]);

angular.module("template/rating/rating.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/rating/rating.html",
    "<span ng-mouseleave=\"reset()\">\n" +
    "    <i ng-repeat=\"r in range\" ng-mouseenter=\"enter($index + 1)\" ng-click=\"rate($index + 1)\" class=\"glyphicon\" ng-class=\"$index < val && (r.stateOn || 'glyphicon-star') || (r.stateOff || 'glyphicon-star-empty')\"></i>\n" +
    "</span>");
}]);

angular.module("template/tabs/tab.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/tabs/tab.html",
    "<li ng-class=\"{active: active, disabled: disabled}\">\n" +
    "  <a ng-click=\"select()\" tab-heading-transclude>{{heading}}</a>\n" +
    "</li>\n" +
    "");
}]);

angular.module("template/tabs/tabset-titles.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/tabs/tabset-titles.html",
    "<ul class=\"nav {{type && 'nav-' + type}}\" ng-class=\"{'nav-stacked': vertical}\">\n" +
    "</ul>\n" +
    "");
}]);

angular.module("template/tabs/tabset.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/tabs/tabset.html",
    "\n" +
    "<div class=\"tabbable\">\n" +
    "  <ul class=\"nav {{type && 'nav-' + type}}\" ng-class=\"{'nav-stacked': vertical, 'nav-justified': justified}\" ng-transclude></ul>\n" +
    "  <div class=\"tab-content\">\n" +
    "    <div class=\"tab-pane\" \n" +
    "         ng-repeat=\"tab in tabs\" \n" +
    "         ng-class=\"{active: tab.active}\"\n" +
    "         tab-content-transclude=\"tab\">\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("template/timepicker/timepicker.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/timepicker/timepicker.html",
    "<table>\n" +
    "	<tbody>\n" +
    "		<tr class=\"text-center\">\n" +
    "			<td><a ng-click=\"incrementHours()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-up\"></span></a></td>\n" +
    "			<td>&nbsp;</td>\n" +
    "			<td><a ng-click=\"incrementMinutes()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-up\"></span></a></td>\n" +
    "			<td ng-show=\"showMeridian\"></td>\n" +
    "		</tr>\n" +
    "		<tr>\n" +
    "			<td style=\"width:50px;\" class=\"form-group\" ng-class=\"{'has-error': invalidHours}\">\n" +
    "				<input type=\"text\" ng-model=\"hours\" ng-change=\"updateHours()\" class=\"form-control text-center\" ng-mousewheel=\"incrementHours()\" ng-readonly=\"readonlyInput\" maxlength=\"2\">\n" +
    "			</td>\n" +
    "			<td>:</td>\n" +
    "			<td style=\"width:50px;\" class=\"form-group\" ng-class=\"{'has-error': invalidMinutes}\">\n" +
    "				<input type=\"text\" ng-model=\"minutes\" ng-change=\"updateMinutes()\" class=\"form-control text-center\" ng-readonly=\"readonlyInput\" maxlength=\"2\">\n" +
    "			</td>\n" +
    "			<td ng-show=\"showMeridian\"><button class=\"btn btn-default text-center\" ng-click=\"toggleMeridian()\">{{meridian}}</button></td>\n" +
    "		</tr>\n" +
    "		<tr class=\"text-center\">\n" +
    "			<td><a ng-click=\"decrementHours()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-down\"></span></a></td>\n" +
    "			<td>&nbsp;</td>\n" +
    "			<td><a ng-click=\"decrementMinutes()\" class=\"btn btn-link\"><span class=\"glyphicon glyphicon-chevron-down\"></span></a></td>\n" +
    "			<td ng-show=\"showMeridian\"></td>\n" +
    "		</tr>\n" +
    "	</tbody>\n" +
    "</table>\n" +
    "");
}]);

angular.module("template/typeahead/typeahead-match.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/typeahead/typeahead-match.html",
    "<a tabindex=\"-1\" bind-html-unsafe=\"match.label | typeaheadHighlight:query\"></a>");
}]);

angular.module("template/typeahead/typeahead-popup.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/typeahead/typeahead-popup.html",
    "<ul class=\"dropdown-menu\" ng-style=\"{display: isOpen()&&'block' || 'none', top: position.top+'px', left: position.left+'px'}\">\n" +
    "    <li ng-repeat=\"match in matches\" ng-class=\"{active: isActive($index) }\" ng-mouseenter=\"selectActive($index)\" ng-click=\"selectMatch($index)\">\n" +
    "        <div typeahead-match index=\"$index\" match=\"match\" query=\"query\" template-url=\"templateUrl\"></div>\n" +
    "    </li>\n" +
    "</ul>");
}]);

/**
 * State-based routing for AngularJS
 * @version v0.2.7
 * @link http://angular-ui.github.com/
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */

/* commonjs package manager support (eg componentjs) */
if (typeof module !== "undefined" && typeof exports !== "undefined" && module.exports === exports){
  module.exports = 'ui.router';
}

(function (window, angular, undefined) {
/*jshint globalstrict:true*/
/*global angular:false*/
'use strict';

var isDefined = angular.isDefined,
    isFunction = angular.isFunction,
    isString = angular.isString,
    isObject = angular.isObject,
    isArray = angular.isArray,
    forEach = angular.forEach,
    extend = angular.extend,
    copy = angular.copy;

function inherit(parent, extra) {
  return extend(new (extend(function() {}, { prototype: parent }))(), extra);
}

function merge(dst) {
  forEach(arguments, function(obj) {
    if (obj !== dst) {
      forEach(obj, function(value, key) {
        if (!dst.hasOwnProperty(key)) dst[key] = value;
      });
    }
  });
  return dst;
}

/**
 * Finds the common ancestor path between two states.
 *
 * @param {Object} first The first state.
 * @param {Object} second The second state.
 * @return {Array} Returns an array of state names in descending order, not including the root.
 */
function ancestors(first, second) {
  var path = [];

  for (var n in first.path) {
    if (first.path[n] === "") continue;
    if (!second.path[n]) break;
    path.push(first.path[n]);
  }
  return path;
}

/**
 * IE8-safe wrapper for `Object.keys()`.
 *
 * @param {Object} object A JavaScript object.
 * @return {Array} Returns the keys of the object as an array.
 */
function keys(object) {
  if (Object.keys) {
    return Object.keys(object);
  }
  var result = [];

  angular.forEach(object, function(val, key) {
    result.push(key);
  });
  return result;
}

/**
 * IE8-safe wrapper for `Array.prototype.indexOf()`.
 *
 * @param {Array} array A JavaScript array.
 * @param {*} value A value to search the array for.
 * @return {Number} Returns the array index value of `value`, or `-1` if not present.
 */
function arraySearch(array, value) {
  if (Array.prototype.indexOf) {
    return array.indexOf(value, Number(arguments[2]) || 0);
  }
  var len = array.length >>> 0, from = Number(arguments[2]) || 0;
  from = (from < 0) ? Math.ceil(from) : Math.floor(from);

  if (from < 0) from += len;

  for (; from < len; from++) {
    if (from in array && array[from] === value) return from;
  }
  return -1;
}

/**
 * Merges a set of parameters with all parameters inherited between the common parents of the
 * current state and a given destination state.
 *
 * @param {Object} currentParams The value of the current state parameters ($stateParams).
 * @param {Object} newParams The set of parameters which will be composited with inherited params.
 * @param {Object} $current Internal definition of object representing the current state.
 * @param {Object} $to Internal definition of object representing state to transition to.
 */
function inheritParams(currentParams, newParams, $current, $to) {
  var parents = ancestors($current, $to), parentParams, inherited = {}, inheritList = [];

  for (var i in parents) {
    if (!parents[i].params || !parents[i].params.length) continue;
    parentParams = parents[i].params;

    for (var j in parentParams) {
      if (arraySearch(inheritList, parentParams[j]) >= 0) continue;
      inheritList.push(parentParams[j]);
      inherited[parentParams[j]] = currentParams[parentParams[j]];
    }
  }
  return extend({}, inherited, newParams);
}

/**
 * Normalizes a set of values to string or `null`, filtering them by a list of keys.
 *
 * @param {Array} keys The list of keys to normalize/return.
 * @param {Object} values An object hash of values to normalize.
 * @return {Object} Returns an object hash of normalized string values.
 */
function normalize(keys, values) {
  var normalized = {};

  forEach(keys, function (name) {
    var value = values[name];
    normalized[name] = (value != null) ? String(value) : null;
  });
  return normalized;
}

/**
 * Performs a non-strict comparison of the subset of two objects, defined by a list of keys.
 *
 * @param {Object} a The first object.
 * @param {Object} b The second object.
 * @param {Array} keys The list of keys within each object to compare. If the list is empty or not specified,
 *                     it defaults to the list of keys in `a`.
 * @return {Boolean} Returns `true` if the keys match, otherwise `false`.
 */
function equalForKeys(a, b, keys) {
  if (!keys) {
    keys = [];
    for (var n in a) keys.push(n); // Used instead of Object.keys() for IE8 compatibility
  }

  for (var i=0; i<keys.length; i++) {
    var k = keys[i];
    if (a[k] != b[k]) return false; // Not '===', values aren't necessarily normalized
  }
  return true;
}

/**
 * Returns the subset of an object, based on a list of keys.
 *
 * @param {Array} keys
 * @param {Object} values
 * @return {Boolean} Returns a subset of `values`.
 */
function filterByKeys(keys, values) {
  var filtered = {};

  forEach(keys, function (name) {
    filtered[name] = values[name];
  });
  return filtered;
}

angular.module('ui.router.util', ['ng']);
angular.module('ui.router.router', ['ui.router.util']);
angular.module('ui.router.state', ['ui.router.router', 'ui.router.util']);
angular.module('ui.router', ['ui.router.state']);
angular.module('ui.router.compat', ['ui.router']);


/**
 * Service (`ui-util`). Manages resolution of (acyclic) graphs of promises.
 * @module $resolve
 * @requires $q
 * @requires $injector
 */
$Resolve.$inject = ['$q', '$injector'];
function $Resolve(  $q,    $injector) {
  
  var VISIT_IN_PROGRESS = 1,
      VISIT_DONE = 2,
      NOTHING = {},
      NO_DEPENDENCIES = [],
      NO_LOCALS = NOTHING,
      NO_PARENT = extend($q.when(NOTHING), { $$promises: NOTHING, $$values: NOTHING });
  

  /**
   * Studies a set of invocables that are likely to be used multiple times.
   *      $resolve.study(invocables)(locals, parent, self)
   * is equivalent to
   *      $resolve.resolve(invocables, locals, parent, self)
   * but the former is more efficient (in fact `resolve` just calls `study` internally).
   * See {@link module:$resolve/resolve} for details.
   * @function
   * @param {Object} invocables
   * @return {Function}
   */
  this.study = function (invocables) {
    if (!isObject(invocables)) throw new Error("'invocables' must be an object");
    
    // Perform a topological sort of invocables to build an ordered plan
    var plan = [], cycle = [], visited = {};
    function visit(value, key) {
      if (visited[key] === VISIT_DONE) return;
      
      cycle.push(key);
      if (visited[key] === VISIT_IN_PROGRESS) {
        cycle.splice(0, cycle.indexOf(key));
        throw new Error("Cyclic dependency: " + cycle.join(" -> "));
      }
      visited[key] = VISIT_IN_PROGRESS;
      
      if (isString(value)) {
        plan.push(key, [ function() { return $injector.get(value); }], NO_DEPENDENCIES);
      } else {
        var params = $injector.annotate(value);
        forEach(params, function (param) {
          if (param !== key && invocables.hasOwnProperty(param)) visit(invocables[param], param);
        });
        plan.push(key, value, params);
      }
      
      cycle.pop();
      visited[key] = VISIT_DONE;
    }
    forEach(invocables, visit);
    invocables = cycle = visited = null; // plan is all that's required
    
    function isResolve(value) {
      return isObject(value) && value.then && value.$$promises;
    }
    
    return function (locals, parent, self) {
      if (isResolve(locals) && self === undefined) {
        self = parent; parent = locals; locals = null;
      }
      if (!locals) locals = NO_LOCALS;
      else if (!isObject(locals)) {
        throw new Error("'locals' must be an object");
      }       
      if (!parent) parent = NO_PARENT;
      else if (!isResolve(parent)) {
        throw new Error("'parent' must be a promise returned by $resolve.resolve()");
      }
      
      // To complete the overall resolution, we have to wait for the parent
      // promise and for the promise for each invokable in our plan.
      var resolution = $q.defer(),
          result = resolution.promise,
          promises = result.$$promises = {},
          values = extend({}, locals),
          wait = 1 + plan.length/3,
          merged = false;
          
      function done() {
        // Merge parent values we haven't got yet and publish our own $$values
        if (!--wait) {
          if (!merged) merge(values, parent.$$values); 
          result.$$values = values;
          result.$$promises = true; // keep for isResolve()
          resolution.resolve(values);
        }
      }
      
      function fail(reason) {
        result.$$failure = reason;
        resolution.reject(reason);
      }
      
      // Short-circuit if parent has already failed
      if (isDefined(parent.$$failure)) {
        fail(parent.$$failure);
        return result;
      }
      
      // Merge parent values if the parent has already resolved, or merge
      // parent promises and wait if the parent resolve is still in progress.
      if (parent.$$values) {
        merged = merge(values, parent.$$values);
        done();
      } else {
        extend(promises, parent.$$promises);
        parent.then(done, fail);
      }
      
      // Process each invocable in the plan, but ignore any where a local of the same name exists.
      for (var i=0, ii=plan.length; i<ii; i+=3) {
        if (locals.hasOwnProperty(plan[i])) done();
        else invoke(plan[i], plan[i+1], plan[i+2]);
      }
      
      function invoke(key, invocable, params) {
        // Create a deferred for this invocation. Failures will propagate to the resolution as well.
        var invocation = $q.defer(), waitParams = 0;
        function onfailure(reason) {
          invocation.reject(reason);
          fail(reason);
        }
        // Wait for any parameter that we have a promise for (either from parent or from this
        // resolve; in that case study() will have made sure it's ordered before us in the plan).
        forEach(params, function (dep) {
          if (promises.hasOwnProperty(dep) && !locals.hasOwnProperty(dep)) {
            waitParams++;
            promises[dep].then(function (result) {
              values[dep] = result;
              if (!(--waitParams)) proceed();
            }, onfailure);
          }
        });
        if (!waitParams) proceed();
        function proceed() {
          if (isDefined(result.$$failure)) return;
          try {
            invocation.resolve($injector.invoke(invocable, self, values));
            invocation.promise.then(function (result) {
              values[key] = result;
              done();
            }, onfailure);
          } catch (e) {
            onfailure(e);
          }
        }
        // Publish promise synchronously; invocations further down in the plan may depend on it.
        promises[key] = invocation.promise;
      }
      
      return result;
    };
  };
  
  /**
   * Resolves a set of invocables. An invocable is a function to be invoked via `$injector.invoke()`,
   * and can have an arbitrary number of dependencies. An invocable can either return a value directly,
   * or a `$q` promise. If a promise is returned it will be resolved and the resulting value will be
   * used instead. Dependencies of invocables are resolved (in this order of precedence)
   *
   * - from the specified `locals`
   * - from another invocable that is part of this `$resolve` call
   * - from an invocable that is inherited from a `parent` call to `$resolve` (or recursively
   *   from any ancestor `$resolve` of that parent).
   *
   * The return value of `$resolve` is a promise for an object that contains (in this order of precedence)
   *
   * - any `locals` (if specified)
   * - the resolved return values of all injectables
   * - any values inherited from a `parent` call to `$resolve` (if specified)
   *
   * The promise will resolve after the `parent` promise (if any) and all promises returned by injectables
   * have been resolved. If any invocable (or `$injector.invoke`) throws an exception, or if a promise
   * returned by an invocable is rejected, the `$resolve` promise is immediately rejected with the same error.
   * A rejection of a `parent` promise (if specified) will likewise be propagated immediately. Once the
   * `$resolve` promise has been rejected, no further invocables will be called.
   * 
   * Cyclic dependencies between invocables are not permitted and will caues `$resolve` to throw an
   * error. As a special case, an injectable can depend on a parameter with the same name as the injectable,
   * which will be fulfilled from the `parent` injectable of the same name. This allows inherited values
   * to be decorated. Note that in this case any other injectable in the same `$resolve` with the same
   * dependency would see the decorated value, not the inherited value.
   *
   * Note that missing dependencies -- unlike cyclic dependencies -- will cause an (asynchronous) rejection
   * of the `$resolve` promise rather than a (synchronous) exception.
   *
   * Invocables are invoked eagerly as soon as all dependencies are available. This is true even for
   * dependencies inherited from a `parent` call to `$resolve`.
   *
   * As a special case, an invocable can be a string, in which case it is taken to be a service name
   * to be passed to `$injector.get()`. This is supported primarily for backwards-compatibility with the
   * `resolve` property of `$routeProvider` routes.
   *
   * @function
   * @param {Object.<string, Function|string>} invocables  functions to invoke or `$injector` services to fetch.
   * @param {Object.<string, *>} [locals]  values to make available to the injectables
   * @param {Promise.<Object>} [parent]  a promise returned by another call to `$resolve`.
   * @param {Object} [self]  the `this` for the invoked methods
   * @return {Promise.<Object>}  Promise for an object that contains the resolved return value
   *    of all invocables, as well as any inherited and local values.
   */
  this.resolve = function (invocables, locals, parent, self) {
    return this.study(invocables)(locals, parent, self);
  };
}

angular.module('ui.router.util').service('$resolve', $Resolve);


/**
 * Service. Manages loading of templates.
 * @constructor
 * @name $templateFactory
 * @requires $http
 * @requires $templateCache
 * @requires $injector
 */
$TemplateFactory.$inject = ['$http', '$templateCache', '$injector'];
function $TemplateFactory(  $http,   $templateCache,   $injector) {

  /**
   * Creates a template from a configuration object. 
   * @function
   * @name $templateFactory#fromConfig
   * @methodOf $templateFactory
   * @param {Object} config  Configuration object for which to load a template. The following
   *    properties are search in the specified order, and the first one that is defined is
   *    used to create the template:
   * @param {string|Function} config.template  html string template or function to load via
   *    {@link $templateFactory#fromString fromString}.
   * @param {string|Function} config.templateUrl  url to load or a function returning the url
   *    to load via {@link $templateFactory#fromUrl fromUrl}.
   * @param {Function} config.templateProvider  function to invoke via
   *    {@link $templateFactory#fromProvider fromProvider}.
   * @param {Object} params  Parameters to pass to the template function.
   * @param {Object} [locals] Locals to pass to `invoke` if the template is loaded via a
   *      `templateProvider`. Defaults to `{ params: params }`.
   * @return {string|Promise.<string>}  The template html as a string, or a promise for that string,
   *      or `null` if no template is configured.
   */
  this.fromConfig = function (config, params, locals) {
    return (
      isDefined(config.template) ? this.fromString(config.template, params) :
      isDefined(config.templateUrl) ? this.fromUrl(config.templateUrl, params) :
      isDefined(config.templateProvider) ? this.fromProvider(config.templateProvider, params, locals) :
      null
    );
  };

  /**
   * Creates a template from a string or a function returning a string.
   * @function
   * @name $templateFactory#fromString
   * @methodOf $templateFactory
   * @param {string|Function} template  html template as a string or function that returns an html
   *      template as a string.
   * @param {Object} params  Parameters to pass to the template function.
   * @return {string|Promise.<string>}  The template html as a string, or a promise for that string.
   */
  this.fromString = function (template, params) {
    return isFunction(template) ? template(params) : template;
  };

  /**
   * Loads a template from the a URL via `$http` and `$templateCache`.
   * @function
   * @name $templateFactory#fromUrl
   * @methodOf $templateFactory
   * @param {string|Function} url  url of the template to load, or a function that returns a url.
   * @param {Object} params  Parameters to pass to the url function.
   * @return {string|Promise.<string>}  The template html as a string, or a promise for that string.
   */
  this.fromUrl = function (url, params) {
    if (isFunction(url)) url = url(params);
    if (url == null) return null;
    else return $http
        .get(url, { cache: $templateCache })
        .then(function(response) { return response.data; });
  };

  /**
   * Creates a template by invoking an injectable provider function.
   * @function
   * @name $templateFactory#fromUrl
   * @methodOf $templateFactory
   * @param {Function} provider Function to invoke via `$injector.invoke`
   * @param {Object} params Parameters for the template.
   * @param {Object} [locals] Locals to pass to `invoke`. Defaults to `{ params: params }`.
   * @return {string|Promise.<string>} The template html as a string, or a promise for that string.
   */
  this.fromProvider = function (provider, params, locals) {
    return $injector.invoke(provider, null, locals || { params: params });
  };
}

angular.module('ui.router.util').service('$templateFactory', $TemplateFactory);

/**
 * Matches URLs against patterns and extracts named parameters from the path or the search
 * part of the URL. A URL pattern consists of a path pattern, optionally followed by '?' and a list
 * of search parameters. Multiple search parameter names are separated by '&'. Search parameters
 * do not influence whether or not a URL is matched, but their values are passed through into
 * the matched parameters returned by {@link UrlMatcher#exec exec}.
 * 
 * Path parameter placeholders can be specified using simple colon/catch-all syntax or curly brace
 * syntax, which optionally allows a regular expression for the parameter to be specified:
 *
 * * ':' name - colon placeholder
 * * '*' name - catch-all placeholder
 * * '{' name '}' - curly placeholder
 * * '{' name ':' regexp '}' - curly placeholder with regexp. Should the regexp itself contain
 *   curly braces, they must be in matched pairs or escaped with a backslash.
 *
 * Parameter names may contain only word characters (latin letters, digits, and underscore) and
 * must be unique within the pattern (across both path and search parameters). For colon 
 * placeholders or curly placeholders without an explicit regexp, a path parameter matches any
 * number of characters other than '/'. For catch-all placeholders the path parameter matches
 * any number of characters.
 * 
 * ### Examples
 * 
 * * '/hello/' - Matches only if the path is exactly '/hello/'. There is no special treatment for
 *   trailing slashes, and patterns have to match the entire path, not just a prefix.
 * * '/user/:id' - Matches '/user/bob' or '/user/1234!!!' or even '/user/' but not '/user' or
 *   '/user/bob/details'. The second path segment will be captured as the parameter 'id'.
 * * '/user/{id}' - Same as the previous example, but using curly brace syntax.
 * * '/user/{id:[^/]*}' - Same as the previous example.
 * * '/user/{id:[0-9a-fA-F]{1,8}}' - Similar to the previous example, but only matches if the id
 *   parameter consists of 1 to 8 hex digits.
 * * '/files/{path:.*}' - Matches any URL starting with '/files/' and captures the rest of the
 *   path into the parameter 'path'.
 * * '/files/*path' - ditto.
 *
 * @constructor
 * @param {string} pattern  the pattern to compile into a matcher.
 *
 * @property {string} prefix  A static prefix of this pattern. The matcher guarantees that any
 *   URL matching this matcher (i.e. any string for which {@link UrlMatcher#exec exec()} returns
 *   non-null) will start with this prefix.
 */
function UrlMatcher(pattern) {

  // Find all placeholders and create a compiled pattern, using either classic or curly syntax:
  //   '*' name
  //   ':' name
  //   '{' name '}'
  //   '{' name ':' regexp '}'
  // The regular expression is somewhat complicated due to the need to allow curly braces
  // inside the regular expression. The placeholder regexp breaks down as follows:
  //    ([:*])(\w+)               classic placeholder ($1 / $2)
  //    \{(\w+)(?:\:( ... ))?\}   curly brace placeholder ($3) with optional regexp ... ($4)
  //    (?: ... | ... | ... )+    the regexp consists of any number of atoms, an atom being either
  //    [^{}\\]+                  - anything other than curly braces or backslash
  //    \\.                       - a backslash escape
  //    \{(?:[^{}\\]+|\\.)*\}     - a matched set of curly braces containing other atoms
  var placeholder = /([:*])(\w+)|\{(\w+)(?:\:((?:[^{}\\]+|\\.|\{(?:[^{}\\]+|\\.)*\})+))?\}/g,
      names = {}, compiled = '^', last = 0, m,
      segments = this.segments = [],
      params = this.params = [];

  function addParameter(id) {
    if (!/^\w+(-+\w+)*$/.test(id)) throw new Error("Invalid parameter name '" + id + "' in pattern '" + pattern + "'");
    if (names[id]) throw new Error("Duplicate parameter name '" + id + "' in pattern '" + pattern + "'");
    names[id] = true;
    params.push(id);
  }

  function quoteRegExp(string) {
    return string.replace(/[\\\[\]\^$*+?.()|{}]/g, "\\$&");
  }

  this.source = pattern;

  // Split into static segments separated by path parameter placeholders.
  // The number of segments is always 1 more than the number of parameters.
  var id, regexp, segment;
  while ((m = placeholder.exec(pattern))) {
    id = m[2] || m[3]; // IE[78] returns '' for unmatched groups instead of null
    regexp = m[4] || (m[1] == '*' ? '.*' : '[^/]*');
    segment = pattern.substring(last, m.index);
    if (segment.indexOf('?') >= 0) break; // we're into the search part
    compiled += quoteRegExp(segment) + '(' + regexp + ')';
    addParameter(id);
    segments.push(segment);
    last = placeholder.lastIndex;
  }
  segment = pattern.substring(last);

  // Find any search parameter names and remove them from the last segment
  var i = segment.indexOf('?');
  if (i >= 0) {
    var search = this.sourceSearch = segment.substring(i);
    segment = segment.substring(0, i);
    this.sourcePath = pattern.substring(0, last+i);

    // Allow parameters to be separated by '?' as well as '&' to make concat() easier
    forEach(search.substring(1).split(/[&?]/), addParameter);
  } else {
    this.sourcePath = pattern;
    this.sourceSearch = '';
  }

  compiled += quoteRegExp(segment) + '$';
  segments.push(segment);
  this.regexp = new RegExp(compiled);
  this.prefix = segments[0];
}

/**
 * Returns a new matcher for a pattern constructed by appending the path part and adding the
 * search parameters of the specified pattern to this pattern. The current pattern is not
 * modified. This can be understood as creating a pattern for URLs that are relative to (or
 * suffixes of) the current pattern.
 *
 * ### Example
 * The following two matchers are equivalent:
 * ```
 * new UrlMatcher('/user/{id}?q').concat('/details?date');
 * new UrlMatcher('/user/{id}/details?q&date');
 * ```
 *
 * @param {string} pattern  The pattern to append.
 * @return {UrlMatcher}  A matcher for the concatenated pattern.
 */
UrlMatcher.prototype.concat = function (pattern) {
  // Because order of search parameters is irrelevant, we can add our own search
  // parameters to the end of the new pattern. Parse the new pattern by itself
  // and then join the bits together, but it's much easier to do this on a string level.
  return new UrlMatcher(this.sourcePath + pattern + this.sourceSearch);
};

UrlMatcher.prototype.toString = function () {
  return this.source;
};

/**
 * Tests the specified path against this matcher, and returns an object containing the captured
 * parameter values, or null if the path does not match. The returned object contains the values
 * of any search parameters that are mentioned in the pattern, but their value may be null if
 * they are not present in `searchParams`. This means that search parameters are always treated
 * as optional.
 *
 * ### Example
 * ```
 * new UrlMatcher('/user/{id}?q&r').exec('/user/bob', { x:'1', q:'hello' });
 * // returns { id:'bob', q:'hello', r:null }
 * ```
 *
 * @param {string} path  The URL path to match, e.g. `$location.path()`.
 * @param {Object} searchParams  URL search parameters, e.g. `$location.search()`.
 * @return {Object}  The captured parameter values.
 */
UrlMatcher.prototype.exec = function (path, searchParams) {
  var m = this.regexp.exec(path);
  if (!m) return null;

  var params = this.params, nTotal = params.length,
    nPath = this.segments.length-1,
    values = {}, i;

  if (nPath !== m.length - 1) throw new Error("Unbalanced capture group in route '" + this.source + "'");

  for (i=0; i<nPath; i++) values[params[i]] = m[i+1];
  for (/**/; i<nTotal; i++) values[params[i]] = searchParams[params[i]];

  return values;
};

/**
 * Returns the names of all path and search parameters of this pattern in an unspecified order.
 * @return {Array.<string>}  An array of parameter names. Must be treated as read-only. If the
 *    pattern has no parameters, an empty array is returned.
 */
UrlMatcher.prototype.parameters = function () {
  return this.params;
};

/**
 * Creates a URL that matches this pattern by substituting the specified values
 * for the path and search parameters. Null values for path parameters are
 * treated as empty strings.
 *
 * ### Example
 * ```
 * new UrlMatcher('/user/{id}?q').format({ id:'bob', q:'yes' });
 * // returns '/user/bob?q=yes'
 * ```
 *
 * @param {Object} values  the values to substitute for the parameters in this pattern.
 * @return {string}  the formatted URL (path and optionally search part).
 */
UrlMatcher.prototype.format = function (values) {
  var segments = this.segments, params = this.params;
  if (!values) return segments.join('');

  var nPath = segments.length-1, nTotal = params.length,
    result = segments[0], i, search, value;

  for (i=0; i<nPath; i++) {
    value = values[params[i]];
    // TODO: Maybe we should throw on null here? It's not really good style to use '' and null interchangeabley
    if (value != null) result += encodeURIComponent(value);
    result += segments[i+1];
  }
  for (/**/; i<nTotal; i++) {
    value = values[params[i]];
    if (value != null) {
      result += (search ? '&' : '?') + params[i] + '=' + encodeURIComponent(value);
      search = true;
    }
  }

  return result;
};

/**
 * Service. Factory for {@link UrlMatcher} instances. The factory is also available to providers
 * under the name `$urlMatcherFactoryProvider`.
 * @constructor
 * @name $urlMatcherFactory
 */
function $UrlMatcherFactory() {
  /**
   * Creates a {@link UrlMatcher} for the specified pattern.
   * @function
   * @name $urlMatcherFactory#compile
   * @methodOf $urlMatcherFactory
   * @param {string} pattern  The URL pattern.
   * @return {UrlMatcher}  The UrlMatcher.
   */
  this.compile = function (pattern) {
    return new UrlMatcher(pattern);
  };

  /**
   * Returns true if the specified object is a UrlMatcher, or false otherwise.
   * @function
   * @name $urlMatcherFactory#isMatcher
   * @methodOf $urlMatcherFactory
   * @param {Object} o
   * @return {boolean}
   */
  this.isMatcher = function (o) {
    return isObject(o) && isFunction(o.exec) && isFunction(o.format) && isFunction(o.concat);
  };

  this.$get = function () {
    return this;
  };
}

// Register as a provider so it's available to other providers
angular.module('ui.router.util').provider('$urlMatcherFactory', $UrlMatcherFactory);


$UrlRouterProvider.$inject = ['$urlMatcherFactoryProvider'];
function $UrlRouterProvider(  $urlMatcherFactory) {
  var rules = [], 
      otherwise = null;

  // Returns a string that is a prefix of all strings matching the RegExp
  function regExpPrefix(re) {
    var prefix = /^\^((?:\\[^a-zA-Z0-9]|[^\\\[\]\^$*+?.()|{}]+)*)/.exec(re.source);
    return (prefix != null) ? prefix[1].replace(/\\(.)/g, "$1") : '';
  }

  // Interpolates matched values into a String.replace()-style pattern
  function interpolate(pattern, match) {
    return pattern.replace(/\$(\$|\d{1,2})/, function (m, what) {
      return match[what === '$' ? 0 : Number(what)];
    });
  }

  this.rule =
    function (rule) {
      if (!isFunction(rule)) throw new Error("'rule' must be a function");
      rules.push(rule);
      return this;
    };

  this.otherwise =
    function (rule) {
      if (isString(rule)) {
        var redirect = rule;
        rule = function () { return redirect; };
      }
      else if (!isFunction(rule)) throw new Error("'rule' must be a function");
      otherwise = rule;
      return this;
    };


  function handleIfMatch($injector, handler, match) {
    if (!match) return false;
    var result = $injector.invoke(handler, handler, { $match: match });
    return isDefined(result) ? result : true;
  }

  this.when =
    function (what, handler) {
      var redirect, handlerIsString = isString(handler);
      if (isString(what)) what = $urlMatcherFactory.compile(what);

      if (!handlerIsString && !isFunction(handler) && !isArray(handler))
        throw new Error("invalid 'handler' in when()");

      var strategies = {
        matcher: function (what, handler) {
          if (handlerIsString) {
            redirect = $urlMatcherFactory.compile(handler);
            handler = ['$match', function ($match) { return redirect.format($match); }];
          }
          return extend(function ($injector, $location) {
            return handleIfMatch($injector, handler, what.exec($location.path(), $location.search()));
          }, {
            prefix: isString(what.prefix) ? what.prefix : ''
          });
        },
        regex: function (what, handler) {
          if (what.global || what.sticky) throw new Error("when() RegExp must not be global or sticky");

          if (handlerIsString) {
            redirect = handler;
            handler = ['$match', function ($match) { return interpolate(redirect, $match); }];
          }
          return extend(function ($injector, $location) {
            return handleIfMatch($injector, handler, what.exec($location.path()));
          }, {
            prefix: regExpPrefix(what)
          });
        }
      };

      var check = { matcher: $urlMatcherFactory.isMatcher(what), regex: what instanceof RegExp };

      for (var n in check) {
        if (check[n]) {
          return this.rule(strategies[n](what, handler));
        }
      }

      throw new Error("invalid 'what' in when()");
    };

  this.$get =
    [        '$location', '$rootScope', '$injector',
    function ($location,   $rootScope,   $injector) {
      // TODO: Optimize groups of rules with non-empty prefix into some sort of decision tree
      function update(evt) {
        if (evt && evt.defaultPrevented) return;
        function check(rule) {
          var handled = rule($injector, $location);
          if (handled) {
            if (isString(handled)) $location.replace().url(handled);
            return true;
          }
          return false;
        }
        var n=rules.length, i;
        for (i=0; i<n; i++) {
          if (check(rules[i])) return;
        }
        // always check otherwise last to allow dynamic updates to the set of rules
        if (otherwise) check(otherwise);
      }

      $rootScope.$on('$locationChangeSuccess', update);

      return {
        sync: function () {
          update();
        }
      };
    }];
}

angular.module('ui.router.router').provider('$urlRouter', $UrlRouterProvider);

$StateProvider.$inject = ['$urlRouterProvider', '$urlMatcherFactoryProvider', '$locationProvider'];
function $StateProvider(   $urlRouterProvider,   $urlMatcherFactory,           $locationProvider) {

  var root, states = {}, $state, queue = {}, abstractKey = 'abstract';

  // Builds state properties from definition passed to registerState()
  var stateBuilder = {

    // Derive parent state from a hierarchical name only if 'parent' is not explicitly defined.
    // state.children = [];
    // if (parent) parent.children.push(state);
    parent: function(state) {
      if (isDefined(state.parent) && state.parent) return findState(state.parent);
      // regex matches any valid composite state name
      // would match "contact.list" but not "contacts"
      var compositeName = /^(.+)\.[^.]+$/.exec(state.name);
      return compositeName ? findState(compositeName[1]) : root;
    },

    // inherit 'data' from parent and override by own values (if any)
    data: function(state) {
      if (state.parent && state.parent.data) {
        state.data = state.self.data = extend({}, state.parent.data, state.data);
      }
      return state.data;
    },

    // Build a URLMatcher if necessary, either via a relative or absolute URL
    url: function(state) {
      var url = state.url;

      if (isString(url)) {
        if (url.charAt(0) == '^') {
          return $urlMatcherFactory.compile(url.substring(1));
        }
        return (state.parent.navigable || root).url.concat(url);
      }

      if ($urlMatcherFactory.isMatcher(url) || url == null) {
        return url;
      }
      throw new Error("Invalid url '" + url + "' in state '" + state + "'");
    },

    // Keep track of the closest ancestor state that has a URL (i.e. is navigable)
    navigable: function(state) {
      return state.url ? state : (state.parent ? state.parent.navigable : null);
    },

    // Derive parameters for this state and ensure they're a super-set of parent's parameters
    params: function(state) {
      if (!state.params) {
        return state.url ? state.url.parameters() : state.parent.params;
      }
      if (!isArray(state.params)) throw new Error("Invalid params in state '" + state + "'");
      if (state.url) throw new Error("Both params and url specicified in state '" + state + "'");
      return state.params;
    },

    // If there is no explicit multi-view configuration, make one up so we don't have
    // to handle both cases in the view directive later. Note that having an explicit
    // 'views' property will mean the default unnamed view properties are ignored. This
    // is also a good time to resolve view names to absolute names, so everything is a
    // straight lookup at link time.
    views: function(state) {
      var views = {};

      forEach(isDefined(state.views) ? state.views : { '': state }, function (view, name) {
        if (name.indexOf('@') < 0) name += '@' + state.parent.name;
        views[name] = view;
      });
      return views;
    },

    ownParams: function(state) {
      if (!state.parent) {
        return state.params;
      }
      var paramNames = {}; forEach(state.params, function (p) { paramNames[p] = true; });

      forEach(state.parent.params, function (p) {
        if (!paramNames[p]) {
          throw new Error("Missing required parameter '" + p + "' in state '" + state.name + "'");
        }
        paramNames[p] = false;
      });
      var ownParams = [];

      forEach(paramNames, function (own, p) {
        if (own) ownParams.push(p);
      });
      return ownParams;
    },

    // Keep a full path from the root down to this state as this is needed for state activation.
    path: function(state) {
      return state.parent ? state.parent.path.concat(state) : []; // exclude root from path
    },

    // Speed up $state.contains() as it's used a lot
    includes: function(state) {
      var includes = state.parent ? extend({}, state.parent.includes) : {};
      includes[state.name] = true;
      return includes;
    },

    $delegates: {}
  };

  function isRelative(stateName) {
    return stateName.indexOf(".") === 0 || stateName.indexOf("^") === 0;
  }

  function findState(stateOrName, base) {
    var isStr = isString(stateOrName),
        name  = isStr ? stateOrName : stateOrName.name,
        path  = isRelative(name);

    if (path) {
      if (!base) throw new Error("No reference point given for path '"  + name + "'");
      var rel = name.split("."), i = 0, pathLength = rel.length, current = base;

      for (; i < pathLength; i++) {
        if (rel[i] === "" && i === 0) {
          current = base;
          continue;
        }
        if (rel[i] === "^") {
          if (!current.parent) throw new Error("Path '" + name + "' not valid for state '" + base.name + "'");
          current = current.parent;
          continue;
        }
        break;
      }
      rel = rel.slice(i).join(".");
      name = current.name + (current.name && rel ? "." : "") + rel;
    }
    var state = states[name];

    if (state && (isStr || (!isStr && (state === stateOrName || state.self === stateOrName)))) {
      return state;
    }
    return undefined;
  }

  function queueState(parentName, state) {
    if (!queue[parentName]) {
      queue[parentName] = [];
    }
    queue[parentName].push(state);
  }

  function registerState(state) {
    // Wrap a new object around the state so we can store our private details easily.
    state = inherit(state, {
      self: state,
      resolve: state.resolve || {},
      toString: function() { return this.name; }
    });

    var name = state.name;
    if (!isString(name) || name.indexOf('@') >= 0) throw new Error("State must have a valid name");
    if (states.hasOwnProperty(name)) throw new Error("State '" + name + "'' is already defined");

    // Get parent name
    var parentName = (name.indexOf('.') !== -1) ? name.substring(0, name.lastIndexOf('.'))
        : (isString(state.parent)) ? state.parent
        : '';

    // If parent is not registered yet, add state to queue and register later
    if (parentName && !states[parentName]) {
      return queueState(parentName, state.self);
    }

    for (var key in stateBuilder) {
      if (isFunction(stateBuilder[key])) state[key] = stateBuilder[key](state, stateBuilder.$delegates[key]);
    }
    states[name] = state;

    // Register the state in the global state list and with $urlRouter if necessary.
    if (!state[abstractKey] && state.url) {
      $urlRouterProvider.when(state.url, ['$match', '$stateParams', function ($match, $stateParams) {
        if ($state.$current.navigable != state || !equalForKeys($match, $stateParams)) {
          $state.transitionTo(state, $match, { location: false });
        }
      }]);
    }

    // Register any queued children
    if (queue[name]) {
      for (var i = 0; i < queue[name].length; i++) {
        registerState(queue[name][i]);
      }
    }

    return state;
  }


  // Implicit root state that is always active
  root = registerState({
    name: '',
    url: '^',
    views: null,
    'abstract': true
  });
  root.navigable = null;


  // .decorator()
  // .decorator(name)
  // .decorator(name, function)
  this.decorator = decorator;
  function decorator(name, func) {
    /*jshint validthis: true */
    if (isString(name) && !isDefined(func)) {
      return stateBuilder[name];
    }
    if (!isFunction(func) || !isString(name)) {
      return this;
    }
    if (stateBuilder[name] && !stateBuilder.$delegates[name]) {
      stateBuilder.$delegates[name] = stateBuilder[name];
    }
    stateBuilder[name] = func;
    return this;
  }

  // .state(state)
  // .state(name, state)
  this.state = state;
  function state(name, definition) {
    /*jshint validthis: true */
    if (isObject(name)) definition = name;
    else definition.name = name;
    registerState(definition);
    return this;
  }

  // $urlRouter is injected just to ensure it gets instantiated
  this.$get = $get;
  $get.$inject = ['$rootScope', '$q', '$view', '$injector', '$resolve', '$stateParams', '$location', '$urlRouter'];
  function $get(   $rootScope,   $q,   $view,   $injector,   $resolve,   $stateParams,   $location,   $urlRouter) {

    var TransitionSuperseded = $q.reject(new Error('transition superseded'));
    var TransitionPrevented = $q.reject(new Error('transition prevented'));
    var TransitionAborted = $q.reject(new Error('transition aborted'));
    var TransitionFailed = $q.reject(new Error('transition failed'));
    var currentLocation = $location.url();

    function syncUrl() {
      if ($location.url() !== currentLocation) {
        $location.url(currentLocation);
        $location.replace();
      }
    }

    root.locals = { resolve: null, globals: { $stateParams: {} } };
    $state = {
      params: {},
      current: root.self,
      $current: root,
      transition: null
    };

    $state.reload = function reload() {
      $state.transitionTo($state.current, $stateParams, { reload: true, inherit: false, notify: false });
    };

    $state.go = function go(to, params, options) {
      return this.transitionTo(to, params, extend({ inherit: true, relative: $state.$current }, options));
    };

    $state.transitionTo = function transitionTo(to, toParams, options) {
      toParams = toParams || {};
      options = extend({
        location: true, inherit: false, relative: null, notify: true, reload: false, $retry: false
      }, options || {});

      var from = $state.$current, fromParams = $state.params, fromPath = from.path;
      var evt, toState = findState(to, options.relative);

      if (!isDefined(toState)) {
        // Broadcast not found event and abort the transition if prevented
        var redirect = { to: to, toParams: toParams, options: options };
        evt = $rootScope.$broadcast('$stateNotFound', redirect, from.self, fromParams);
        if (evt.defaultPrevented) {
          syncUrl();
          return TransitionAborted;
        }

        // Allow the handler to return a promise to defer state lookup retry
        if (evt.retry) {
          if (options.$retry) {
            syncUrl();
            return TransitionFailed;
          }
          var retryTransition = $state.transition = $q.when(evt.retry);
          retryTransition.then(function() {
            if (retryTransition !== $state.transition) return TransitionSuperseded;
            redirect.options.$retry = true;
            return $state.transitionTo(redirect.to, redirect.toParams, redirect.options);
          }, function() {
            return TransitionAborted;
          });
          syncUrl();
          return retryTransition;
        }

        // Always retry once if the $stateNotFound was not prevented
        // (handles either redirect changed or state lazy-definition)
        to = redirect.to;
        toParams = redirect.toParams;
        options = redirect.options;
        toState = findState(to, options.relative);
        if (!isDefined(toState)) {
          if (options.relative) throw new Error("Could not resolve '" + to + "' from state '" + options.relative + "'");
          throw new Error("No such state '" + to + "'");
        }
      }
      if (toState[abstractKey]) throw new Error("Cannot transition to abstract state '" + to + "'");
      if (options.inherit) toParams = inheritParams($stateParams, toParams || {}, $state.$current, toState);
      to = toState;

      var toPath = to.path;

      // Starting from the root of the path, keep all levels that haven't changed
      var keep, state, locals = root.locals, toLocals = [];
      for (keep = 0, state = toPath[keep];
           state && state === fromPath[keep] && equalForKeys(toParams, fromParams, state.ownParams) && !options.reload;
           keep++, state = toPath[keep]) {
        locals = toLocals[keep] = state.locals;
      }

      // If we're going to the same state and all locals are kept, we've got nothing to do.
      // But clear 'transition', as we still want to cancel any other pending transitions.
      // TODO: We may not want to bump 'transition' if we're called from a location change that we've initiated ourselves,
      // because we might accidentally abort a legitimate transition initiated from code?
      if (shouldTriggerReload(to, from, locals, options) ) {
        if ( to.self.reloadOnSearch !== false )
          syncUrl();
        $state.transition = null;
        return $q.when($state.current);
      }

      // Normalize/filter parameters before we pass them to event handlers etc.
      toParams = normalize(to.params, toParams || {});

      // Broadcast start event and cancel the transition if requested
      if (options.notify) {
        evt = $rootScope.$broadcast('$stateChangeStart', to.self, toParams, from.self, fromParams);
        if (evt.defaultPrevented) {
          syncUrl();
          return TransitionPrevented;
        }
      }

      // Resolve locals for the remaining states, but don't update any global state just
      // yet -- if anything fails to resolve the current state needs to remain untouched.
      // We also set up an inheritance chain for the locals here. This allows the view directive
      // to quickly look up the correct definition for each view in the current state. Even
      // though we create the locals object itself outside resolveState(), it is initially
      // empty and gets filled asynchronously. We need to keep track of the promise for the
      // (fully resolved) current locals, and pass this down the chain.
      var resolved = $q.when(locals);
      for (var l=keep; l<toPath.length; l++, state=toPath[l]) {
        locals = toLocals[l] = inherit(locals);
        resolved = resolveState(state, toParams, state===to, resolved, locals);
      }

      // Once everything is resolved, we are ready to perform the actual transition
      // and return a promise for the new state. We also keep track of what the
      // current promise is, so that we can detect overlapping transitions and
      // keep only the outcome of the last transition.
      var transition = $state.transition = resolved.then(function () {
        var l, entering, exiting;

        if ($state.transition !== transition) return TransitionSuperseded;

        // Exit 'from' states not kept
        for (l=fromPath.length-1; l>=keep; l--) {
          exiting = fromPath[l];
          if (exiting.self.onExit) {
            $injector.invoke(exiting.self.onExit, exiting.self, exiting.locals.globals);
          }
          exiting.locals = null;
        }

        // Enter 'to' states not kept
        for (l=keep; l<toPath.length; l++) {
          entering = toPath[l];
          entering.locals = toLocals[l];
          if (entering.self.onEnter) {
            $injector.invoke(entering.self.onEnter, entering.self, entering.locals.globals);
          }
        }

        // Run it again, to catch any transitions in callbacks
        if ($state.transition !== transition) return TransitionSuperseded;

        // Update globals in $state
        $state.$current = to;
        $state.current = to.self;
        $state.params = toParams;
        copy($state.params, $stateParams);
        $state.transition = null;

        // Update $location
        var toNav = to.navigable;
        if (options.location && toNav) {
          $location.url(toNav.url.format(toNav.locals.globals.$stateParams));

          if (options.location === 'replace') {
            $location.replace();
          }
        }

        if (options.notify) {
          $rootScope.$broadcast('$stateChangeSuccess', to.self, toParams, from.self, fromParams);
        }
        currentLocation = $location.url();

        return $state.current;
      }, function (error) {
        if ($state.transition !== transition) return TransitionSuperseded;

        $state.transition = null;
        $rootScope.$broadcast('$stateChangeError', to.self, toParams, from.self, fromParams, error);
        syncUrl();

        return $q.reject(error);
      });

      return transition;
    };

    $state.is = function is(stateOrName, params) {
      var state = findState(stateOrName);

      if (!isDefined(state)) {
        return undefined;
      }

      if ($state.$current !== state) {
        return false;
      }

      return isDefined(params) ? angular.equals($stateParams, params) : true;
    };

    $state.includes = function includes(stateOrName, params) {
      var state = findState(stateOrName);
      if (!isDefined(state)) {
        return undefined;
      }

      if (!isDefined($state.$current.includes[state.name])) {
        return false;
      }

      var validParams = true;
      angular.forEach(params, function(value, key) {
        if (!isDefined($stateParams[key]) || $stateParams[key] !== value) {
          validParams = false;
        }
      });
      return validParams;
    };

    $state.href = function href(stateOrName, params, options) {
      options = extend({ lossy: true, inherit: false, absolute: false, relative: $state.$current }, options || {});
      var state = findState(stateOrName, options.relative);
      if (!isDefined(state)) return null;

      params = inheritParams($stateParams, params || {}, $state.$current, state);
      var nav = (state && options.lossy) ? state.navigable : state;
      var url = (nav && nav.url) ? nav.url.format(normalize(state.params, params || {})) : null;
      if (!$locationProvider.html5Mode() && url) {
        url = "#" + $locationProvider.hashPrefix() + url;
      }
      if (options.absolute && url) {
        url = $location.protocol() + '://' + 
              $location.host() + 
              ($location.port() == 80 || $location.port() == 443 ? '' : ':' + $location.port()) + 
              (!$locationProvider.html5Mode() && url ? '/' : '') + 
              url;
      }
      return url;
    };

    $state.get = function (stateOrName, context) {
      if (!isDefined(stateOrName)) {
        var list = [];
        forEach(states, function(state) { list.push(state.self); });
        return list;
      }
      var state = findState(stateOrName, context);
      return (state && state.self) ? state.self : null;
    };

    function resolveState(state, params, paramsAreFiltered, inherited, dst) {
      // Make a restricted $stateParams with only the parameters that apply to this state if
      // necessary. In addition to being available to the controller and onEnter/onExit callbacks,
      // we also need $stateParams to be available for any $injector calls we make during the
      // dependency resolution process.
      var $stateParams = (paramsAreFiltered) ? params : filterByKeys(state.params, params);
      var locals = { $stateParams: $stateParams };

      // Resolve 'global' dependencies for the state, i.e. those not specific to a view.
      // We're also including $stateParams in this; that way the parameters are restricted
      // to the set that should be visible to the state, and are independent of when we update
      // the global $state and $stateParams values.
      dst.resolve = $resolve.resolve(state.resolve, locals, dst.resolve, state);
      var promises = [ dst.resolve.then(function (globals) {
        dst.globals = globals;
      }) ];
      if (inherited) promises.push(inherited);

      // Resolve template and dependencies for all views.
      forEach(state.views, function (view, name) {
        var injectables = (view.resolve && view.resolve !== state.resolve ? view.resolve : {});
        injectables.$template = [ function () {
          return $view.load(name, { view: view, locals: locals, params: $stateParams, notify: false }) || '';
        }];

        promises.push($resolve.resolve(injectables, locals, dst.resolve, state).then(function (result) {
          // References to the controller (only instantiated at link time)
          if (isFunction(view.controllerProvider) || isArray(view.controllerProvider)) {
            var injectLocals = angular.extend({}, injectables, locals);
            result.$$controller = $injector.invoke(view.controllerProvider, null, injectLocals);
          } else {
            result.$$controller = view.controller;
          }
          // Provide access to the state itself for internal use
          result.$$state = state;
          dst[name] = result;
        }));
      });

      // Wait for all the promises and then return the activation object
      return $q.all(promises).then(function (values) {
        return dst;
      });
    }

    return $state;
  }

  function shouldTriggerReload(to, from, locals, options) {
    if ( to === from && ((locals === from.locals && !options.reload) || (to.self.reloadOnSearch === false)) ) {
      return true;
    }
  }
}

angular.module('ui.router.state')
  .value('$stateParams', {})
  .provider('$state', $StateProvider);


$ViewProvider.$inject = [];
function $ViewProvider() {

  this.$get = $get;
  $get.$inject = ['$rootScope', '$templateFactory'];
  function $get(   $rootScope,   $templateFactory) {
    return {
      // $view.load('full.viewName', { template: ..., controller: ..., resolve: ..., async: false, params: ... })
      load: function load(name, options) {
        var result, defaults = {
          template: null, controller: null, view: null, locals: null, notify: true, async: true, params: {}
        };
        options = extend(defaults, options);

        if (options.view) {
          result = $templateFactory.fromConfig(options.view, options.params, options.locals);
        }
        if (result && options.notify) {
          $rootScope.$broadcast('$viewContentLoading', options);
        }
        return result;
      }
    };
  }
}

angular.module('ui.router.state').provider('$view', $ViewProvider);


$ViewDirective.$inject = ['$state', '$compile', '$controller', '$injector', '$anchorScroll'];
function $ViewDirective(   $state,   $compile,   $controller,   $injector,   $anchorScroll) {
  var $animator = $injector.has('$animator') ? $injector.get('$animator') : false;
  var viewIsUpdating = false;

  var directive = {
    restrict: 'ECA',
    terminal: true,
    priority: 1000,
    transclude: true,
    compile: function (element, attr, transclude) {
      return function(scope, element, attr) {
        var viewScope, viewLocals,
            name = attr[directive.name] || attr.name || '',
            onloadExp = attr.onload || '',
            animate = $animator && $animator(scope, attr),
            initialView = transclude(scope);

        // Returns a set of DOM manipulation functions based on whether animation
        // should be performed
        var renderer = function(doAnimate) {
          return ({
            "true": {
              remove: function(element) { animate.leave(element.contents(), element); },
              restore: function(compiled, element) { animate.enter(compiled, element); },
              populate: function(template, element) {
                var contents = angular.element('<div></div>').html(template).contents();
                animate.enter(contents, element);
                return contents;
              }
            },
            "false": {
              remove: function(element) { element.html(''); },
              restore: function(compiled, element) { element.append(compiled); },
              populate: function(template, element) {
                element.html(template);
                return element.contents();
              }
            }
          })[doAnimate.toString()];
        };

        // Put back the compiled initial view
        element.append(initialView);

        // Find the details of the parent view directive (if any) and use it
        // to derive our own qualified view name, then hang our own details
        // off the DOM so child directives can find it.
        var parent = element.parent().inheritedData('$uiView');
        if (name.indexOf('@') < 0) name  = name + '@' + (parent ? parent.state.name : '');
        var view = { name: name, state: null };
        element.data('$uiView', view);

        var eventHook = function() {
          if (viewIsUpdating) return;
          viewIsUpdating = true;

          try { updateView(true); } catch (e) {
            viewIsUpdating = false;
            throw e;
          }
          viewIsUpdating = false;
        };

        scope.$on('$stateChangeSuccess', eventHook);
        scope.$on('$viewContentLoading', eventHook);
        updateView(false);

        function updateView(doAnimate) {
          var locals = $state.$current && $state.$current.locals[name];
          if (locals === viewLocals) return; // nothing to do
          var render = renderer(animate && doAnimate);

          // Remove existing content
          render.remove(element);

          // Destroy previous view scope
          if (viewScope) {
            viewScope.$destroy();
            viewScope = null;
          }

          if (!locals) {
            viewLocals = null;
            view.state = null;

            // Restore the initial view
            return render.restore(initialView, element);
          }

          viewLocals = locals;
          view.state = locals.$$state;

          var link = $compile(render.populate(locals.$template, element));
          viewScope = scope.$new();

          if (locals.$$controller) {
            locals.$scope = viewScope;
            var controller = $controller(locals.$$controller, locals);
            element.children().data('$ngControllerController', controller);
          }
          link(viewScope);
          viewScope.$emit('$viewContentLoaded');
          if (onloadExp) viewScope.$eval(onloadExp);

          // TODO: This seems strange, shouldn't $anchorScroll listen for $viewContentLoaded if necessary?
          // $anchorScroll might listen on event...
          $anchorScroll();
        }
      };
    }
  };
  return directive;
}

angular.module('ui.router.state').directive('uiView', $ViewDirective);

function parseStateRef(ref) {
  var parsed = ref.replace(/\n/g, " ").match(/^([^(]+?)\s*(\((.*)\))?$/);
  if (!parsed || parsed.length !== 4) throw new Error("Invalid state ref '" + ref + "'");
  return { state: parsed[1], paramExpr: parsed[3] || null };
}

function stateContext(el) {
  var stateData = el.parent().inheritedData('$uiView');

  if (stateData && stateData.state && stateData.state.name) {
    return stateData.state;
  }
}

$StateRefDirective.$inject = ['$state', '$timeout'];
function $StateRefDirective($state, $timeout) {
  return {
    restrict: 'A',
    require: '?^uiSrefActive',
    link: function(scope, element, attrs, uiSrefActive) {
      var ref = parseStateRef(attrs.uiSref);
      var params = null, url = null, base = stateContext(element) || $state.$current;
      var isForm = element[0].nodeName === "FORM";
      var attr = isForm ? "action" : "href", nav = true;

      var update = function(newVal) {
        if (newVal) params = newVal;
        if (!nav) return;

        var newHref = $state.href(ref.state, params, { relative: base });

        if (!newHref) {
          nav = false;
          return false;
        }
        element[0][attr] = newHref;
        if (uiSrefActive) {
          uiSrefActive.$$setStateInfo(ref.state, params);
        }
      };

      if (ref.paramExpr) {
        scope.$watch(ref.paramExpr, function(newVal, oldVal) {
          if (newVal !== params) update(newVal);
        }, true);
        params = scope.$eval(ref.paramExpr);
      }
      update();

      if (isForm) return;

      element.bind("click", function(e) {
        var button = e.which || e.button;

        if ((button === 0 || button == 1) && !e.ctrlKey && !e.metaKey && !e.shiftKey) {
          // HACK: This is to allow ng-clicks to be processed before the transition is initiated:
          $timeout(function() {
            scope.$apply(function() {
              $state.go(ref.state, params, { relative: base });
            });
          });
          e.preventDefault();
        }
      });
    }
  };
}

$StateActiveDirective.$inject = ['$state', '$stateParams', '$interpolate'];
function $StateActiveDirective($state, $stateParams, $interpolate) {
  return {
    restrict: "A",
    controller: ["$scope", "$element", "$attrs", function($scope, $element, $attrs) {
      var state, params, activeClass;

      // There probably isn't much point in $observing this
      activeClass = $interpolate($attrs.uiSrefActive || '', false)($scope);

      // Allow uiSref to communicate with uiSrefActive
      this.$$setStateInfo = function(newState, newParams) {
        state = $state.get(newState, stateContext($element));
        params = newParams;
        update();
      };

      $scope.$on('$stateChangeSuccess', update);

      // Update route state
      function update() {
        if ($state.$current.self === state && matchesParams()) {
          $element.addClass(activeClass);
        } else {
          $element.removeClass(activeClass);
        }
      }

      function matchesParams() {
        return !params || equalForKeys(params, $stateParams);
      }
    }]
  };
}

angular.module('ui.router.state')
  .directive('uiSref', $StateRefDirective)
  .directive('uiSrefActive', $StateActiveDirective);

$RouteProvider.$inject = ['$stateProvider', '$urlRouterProvider'];
function $RouteProvider(  $stateProvider,    $urlRouterProvider) {

  var routes = [];

  onEnterRoute.$inject = ['$$state'];
  function onEnterRoute(   $$state) {
    /*jshint validthis: true */
    this.locals = $$state.locals.globals;
    this.params = this.locals.$stateParams;
  }

  function onExitRoute() {
    /*jshint validthis: true */
    this.locals = null;
    this.params = null;
  }

  this.when = when;
  function when(url, route) {
    /*jshint validthis: true */
    if (route.redirectTo != null) {
      // Redirect, configure directly on $urlRouterProvider
      var redirect = route.redirectTo, handler;
      if (isString(redirect)) {
        handler = redirect; // leave $urlRouterProvider to handle
      } else if (isFunction(redirect)) {
        // Adapt to $urlRouterProvider API
        handler = function (params, $location) {
          return redirect(params, $location.path(), $location.search());
        };
      } else {
        throw new Error("Invalid 'redirectTo' in when()");
      }
      $urlRouterProvider.when(url, handler);
    } else {
      // Regular route, configure as state
      $stateProvider.state(inherit(route, {
        parent: null,
        name: 'route:' + encodeURIComponent(url),
        url: url,
        onEnter: onEnterRoute,
        onExit: onExitRoute
      }));
    }
    routes.push(route);
    return this;
  }

  this.$get = $get;
  $get.$inject = ['$state', '$rootScope', '$routeParams'];
  function $get(   $state,   $rootScope,   $routeParams) {

    var $route = {
      routes: routes,
      params: $routeParams,
      current: undefined
    };

    function stateAsRoute(state) {
      return (state.name !== '') ? state : undefined;
    }

    $rootScope.$on('$stateChangeStart', function (ev, to, toParams, from, fromParams) {
      $rootScope.$broadcast('$routeChangeStart', stateAsRoute(to), stateAsRoute(from));
    });

    $rootScope.$on('$stateChangeSuccess', function (ev, to, toParams, from, fromParams) {
      $route.current = stateAsRoute(to);
      $rootScope.$broadcast('$routeChangeSuccess', stateAsRoute(to), stateAsRoute(from));
      copy(toParams, $route.params);
    });

    $rootScope.$on('$stateChangeError', function (ev, to, toParams, from, fromParams, error) {
      $rootScope.$broadcast('$routeChangeError', stateAsRoute(to), stateAsRoute(from), error);
    });

    return $route;
  }
}

angular.module('ui.router.compat')
  .provider('$route', $RouteProvider)
  .directive('ngView', $ViewDirective);
})(window, window.angular);
/***********************************************
* ng-grid JavaScript Library
* Authors: https://github.com/angular-ui/ng-grid/blob/master/README.md 
* License: MIT (http://www.opensource.org/licenses/mit-license.php)
* Compiled At: 03/25/2015 18:13
***********************************************/
(function(window, $) {
'use strict';
// the # of rows we want to add to the top and bottom of the rendered grid rows 
var EXCESS_ROWS = 6;
var SCROLL_THRESHOLD = 4;
var ASC = "asc";
// constant for sorting direction
var DESC = "desc";
// constant for sorting direction
var NG_FIELD = '_ng_field_';
var NG_DEPTH = '_ng_depth_';
var NG_HIDDEN = '_ng_hidden_';
var NG_COLUMN = '_ng_column_';
var CUSTOM_FILTERS = /CUSTOM_FILTERS/g;
var COL_FIELD = /COL_FIELD/g;
var DISPLAY_CELL_TEMPLATE = /DISPLAY_CELL_TEMPLATE/g;
var EDITABLE_CELL_TEMPLATE = /EDITABLE_CELL_TEMPLATE/g;
var TEMPLATE_REGEXP = /<.+>/;
window.ngGrid = {};
window.ngGrid.i18n = {};

// Declare app level module which depends on filters, and services
var ngGridServices = angular.module('ngGrid.services', []);
var ngGridDirectives = angular.module('ngGrid.directives', []);
var ngGridFilters = angular.module('ngGrid.filters', []);
// initialization of services into the main module
angular.module('ngGrid', ['ngGrid.services', 'ngGrid.directives', 'ngGrid.filters']);
//set event binding on the grid so we can select using the up/down keys
var ngMoveSelectionHandler = function($scope, elm, evt, grid) {
    if ($scope.selectionProvider.selectedItems === undefined) {
        return true;
    }

    var charCode = evt.which || evt.keyCode,
        newColumnIndex,
        lastInRow = false,
        firstInRow = false,
        rowIndex = $scope.selectionProvider.lastClickedRow === undefined ? 1 : $scope.selectionProvider.lastClickedRow.rowIndex,
        visibleCols = $scope.columns.filter(function(c) { return c.visible; }),
        pinnedCols = $scope.columns.filter(function(c) { return c.pinned; });

    if ($scope.col) {
        newColumnIndex = visibleCols.indexOf($scope.col);
    }

    if (charCode !== 37 && charCode !== 38 && charCode !== 39 && charCode !== 40 && charCode !== 9 && charCode !== 13) {
        return true;
    }
    
    if ($scope.enableCellSelection) {
        if (charCode === 9) { //tab key
            evt.preventDefault();
        }

        var focusedOnFirstColumn = $scope.showSelectionCheckbox ? $scope.col.index === 1 : $scope.col.index === 0;
        var focusedOnFirstVisibleColumns = $scope.$index === 1 || $scope.$index === 0;
        var focusedOnLastVisibleColumns = $scope.$index === ($scope.renderedColumns.length - 1) || $scope.$index === ($scope.renderedColumns.length - 2);
        var focusedOnLastColumn = visibleCols.indexOf($scope.col) === (visibleCols.length - 1);
        var focusedOnLastPinnedColumn = pinnedCols.indexOf($scope.col) === (pinnedCols.length - 1);
        
        if (charCode === 37 || charCode === 9 && evt.shiftKey) {
            var scrollTo = 0;

            if (!focusedOnFirstColumn) {
                newColumnIndex -= 1;
            }

            if (focusedOnFirstVisibleColumns) {
                if (focusedOnFirstColumn && charCode === 9 && evt.shiftKey){
                    scrollTo = grid.$canvas.width();
                    newColumnIndex = visibleCols.length - 1;
                    firstInRow = true;
                }
                else {
                    scrollTo = grid.$viewport.scrollLeft() - $scope.col.width;
                }
            }
            else if (pinnedCols.length > 0) {
                scrollTo = grid.$viewport.scrollLeft() - visibleCols[newColumnIndex].width;
            }

            grid.$viewport.scrollLeft(scrollTo);
        
        }
        else if (charCode === 39 || charCode ===  9 && !evt.shiftKey) {
            if (focusedOnLastVisibleColumns) {
                if (focusedOnLastColumn && charCode ===  9 && !evt.shiftKey) {
                    grid.$viewport.scrollLeft(0);
                    newColumnIndex = $scope.showSelectionCheckbox ? 1 : 0;  
                    lastInRow = true;
                }
                else {
                    grid.$viewport.scrollLeft(grid.$viewport.scrollLeft() + $scope.col.width);
                }
            }
            else if (focusedOnLastPinnedColumn) {
                grid.$viewport.scrollLeft(0);
            }

            if (!focusedOnLastColumn) {
                newColumnIndex += 1;
            }
        }
    }
  
    var items;
    if ($scope.configGroups.length > 0) {
        items = grid.rowFactory.parsedData.filter(function (row) {
            return !row.isAggRow;
        });
    }
    else {
        items = grid.filteredRows;
    }
    
    var offset = 0;
    if (rowIndex !== 0 && (charCode === 38 || charCode === 13 && evt.shiftKey || charCode === 9 && evt.shiftKey && firstInRow)) { //arrow key up or shift enter or tab key and first item in row
        offset = -1;
    }
    else if (rowIndex !== items.length - 1 && (charCode === 40 || charCode === 13 && !evt.shiftKey || charCode === 9 && lastInRow)) {//arrow key down, enter, or tab key and last item in row?
        offset = 1;
    }
    
    if (offset) {
        var r = items[rowIndex + offset];
        if (r.beforeSelectionChange(r, evt)) {
            r.continueSelection(evt);
            $scope.$emit('ngGridEventDigestGridParent');

            if ($scope.selectionProvider.lastClickedRow.renderedRowIndex >= $scope.renderedRows.length - EXCESS_ROWS - 2) {
                grid.$viewport.scrollTop(grid.$viewport.scrollTop() + $scope.rowHeight);
            }
            else if ($scope.selectionProvider.lastClickedRow.renderedRowIndex <= EXCESS_ROWS + 2) {
                grid.$viewport.scrollTop(grid.$viewport.scrollTop() - $scope.rowHeight);
            }
      }
    }
    
    if ($scope.enableCellSelection) {
        setTimeout(function(){
            $scope.domAccessProvider.focusCellElement($scope, $scope.renderedColumns.indexOf(visibleCols[newColumnIndex]));
        }, 3);
    }

    return false;
};

if (!String.prototype.trim) {
    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, '');
    };
}
if (!Array.prototype.indexOf) {
    Array.prototype.indexOf = function(elt /*, from*/) {
        var len = this.length >>> 0;
        var from = Number(arguments[1]) || 0;
        from = (from < 0) ? Math.ceil(from) : Math.floor(from);
        if (from < 0) {
            from += len;
        }
        for (; from < len; from++) {
            if (from in this && this[from] === elt) {
                return from;
            }
        }
        return -1;
    };
}
if (!Array.prototype.filter) {
    Array.prototype.filter = function(fun /*, thisp */) {
        "use strict";
        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun !== "function") {
            throw new TypeError();
        }
        var res = [];
        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in t) {
                var val = t[i]; // in case fun mutates this
                if (fun.call(thisp, val, i, t)) {
                    res.push(val);
                }
            }
        }
        return res;
    };
}
ngGridFilters.filter('checkmark', function() {
    return function(input) {
        return input ? '\u2714' : '\u2718';
    };
});
ngGridFilters.filter('ngColumns', function() {
    return function(input) {
        return input.filter(function(col) {
            return !col.isAggCol;
        });
    };
});
angular.module('ngGrid.services').factory('$domUtilityService',['$utilityService', function($utils) {
    var domUtilityService = {};
    var regexCache = {};
    var getWidths = function() {
        var $testContainer = $('<div></div>');
        $testContainer.appendTo('body');
        // 1. Run all the following measurements on startup!
        //measure Scroll Bars
        $testContainer.height(100).width(100).css("position", "absolute").css("overflow", "scroll");
        $testContainer.append('<div style="height: 400px; width: 400px;"></div>');
        domUtilityService.ScrollH = ($testContainer.height() - $testContainer[0].clientHeight);
        domUtilityService.ScrollW = ($testContainer.width() - $testContainer[0].clientWidth);
        $testContainer.empty();
        //clear styles
        $testContainer.attr('style', '');
        //measure letter sizes using a pretty typical font size and fat font-family
        $testContainer.append('<span style="font-family: Verdana, Helvetica, Sans-Serif; font-size: 14px;"><strong>M</strong></span>');
        domUtilityService.LetterW = $testContainer.children().first().width();
        $testContainer.remove();
    };
    domUtilityService.eventStorage = {};
    domUtilityService.AssignGridContainers = function($scope, rootEl, grid) {
        grid.$root = $(rootEl);
        //Headers
        grid.$topPanel = grid.$root.find(".ngTopPanel");
        grid.$groupPanel = grid.$root.find(".ngGroupPanel");
        grid.$headerContainer = grid.$topPanel.find(".ngHeaderContainer");
        $scope.$headerContainer = grid.$headerContainer;

        grid.$headerScroller = grid.$topPanel.find(".ngHeaderScroller");
        grid.$headers = grid.$headerScroller.children();
        //Viewport
        grid.$viewport = grid.$root.find(".ngViewport");
        //Canvas
        grid.$canvas = grid.$viewport.find(".ngCanvas");
        //Footers
        grid.$footerPanel = grid.$root.find(".ngFooterPanel");
        
        $scope.$watch(function () {
            return grid.$viewport.scrollLeft();
        }, function (newLeft) {
            return grid.$headerContainer.scrollLeft(newLeft);
        });
        domUtilityService.UpdateGridLayout($scope, grid);
    };
    domUtilityService.getRealWidth = function (obj) {
        var width = 0;
        var props = { visibility: "hidden", display: "block" };
        var hiddenParents = obj.parents().andSelf().not(':visible');
        $.swap(hiddenParents[0], props, function () {
            width = obj.outerWidth();
        });
        return width;
    };
    domUtilityService.UpdateGridLayout = function($scope, grid) {
        //catch this so we can return the viewer to their original scroll after the resize!
        var scrollTop = grid.$viewport.scrollTop();
        grid.elementDims.rootMaxW = grid.$root.width();
        if (grid.$root.is(':hidden')) {
            grid.elementDims.rootMaxW = domUtilityService.getRealWidth(grid.$root);
        }
        grid.elementDims.rootMaxH = grid.$root.height();
        //check to see if anything has changed
        grid.refreshDomSizes();
        $scope.adjustScrollTop(scrollTop, true); //ensure that the user stays scrolled where they were
    };
    domUtilityService.numberOfGrids = 0;
    domUtilityService.BuildStyles = function($scope, grid, digest) {
        var rowHeight = grid.config.rowHeight,
            $style = grid.$styleSheet,
            gridId = grid.gridId,
            css,
            cols = $scope.columns,
            sumWidth = 0;

        if (!$style) {
            $style = $('#' + gridId);
            if (!$style[0]) {
                $style = $("<style id='" + gridId + "' type='text/css' rel='stylesheet' />").appendTo(grid.$root);
            }
        }
        $style.empty();
        var trw = $scope.totalRowWidth();
        css = "." + gridId + " .ngCanvas { width: " + trw + "px; }" +
            "." + gridId + " .ngRow { width: " + trw + "px; }" +
            "." + gridId + " .ngCanvas { width: " + trw + "px; }" +
            "." + gridId + " .ngHeaderScroller { width: " + (trw + domUtilityService.ScrollH) + "px}";

        for (var i = 0; i < cols.length; i++) {
            var col = cols[i];
            if (col.visible !== false) {
                css += "." + gridId + " .col" + i + " { width: " + col.width + "px; left: " + sumWidth + "px; height: " + rowHeight + "px }" +
                    "." + gridId + " .colt" + i + " { width: " + col.width + "px; }";
                sumWidth += col.width;
            }
        }

        if ($utils.isIe) { // IE
            $style[0].styleSheet.cssText = css;
        }

        else {
            $style[0].appendChild(document.createTextNode(css));
        }

        grid.$styleSheet = $style;
        
        $scope.adjustScrollLeft(grid.$viewport.scrollLeft());
        if (digest) {
            domUtilityService.digest($scope);
        }
    };
    domUtilityService.setColLeft = function(col, colLeft, grid) {
        if (grid.$styleSheet) {
            var regex = regexCache[col.index];
            if (!regex) {
                regex = regexCache[col.index] = new RegExp(".col" + col.index + " { width: [0-9]+px; left: [0-9]+px");
            }
            var str = grid.$styleSheet.html();
            var newStr = str.replace(regex, ".col" + col.index + " { width: " + col.width + "px; left: " + colLeft + "px");
            if ($utils.isIe) { // IE
                setTimeout(function() {
                    grid.$styleSheet.html(newStr);
                });
            }
            else {
                grid.$styleSheet.html(newStr);
            }
        }
    };
    domUtilityService.setColLeft.immediate = 1;
    domUtilityService.RebuildGrid = function($scope, grid){
        domUtilityService.UpdateGridLayout($scope, grid);
        if (grid.config.maintainColumnRatios == null || grid.config.maintainColumnRatios) {
            grid.configureColumnWidths();
        }
        $scope.adjustScrollLeft(grid.$viewport.scrollLeft());
        domUtilityService.BuildStyles($scope, grid, true);
    };

    domUtilityService.digest = function($scope) {
        if (!$scope.$root.$$phase) {
            $scope.$digest();
        }
    };
    domUtilityService.ScrollH = 17; // default in IE, Chrome, & most browsers
    domUtilityService.ScrollW = 17; // default in IE, Chrome, & most browsers
    domUtilityService.LetterW = 10;
    getWidths();
    return domUtilityService;
}]);
angular.module('ngGrid.services').factory('$sortService', ['$parse', function($parse) {
    var sortService = {};
    sortService.colSortFnCache = {}; // cache of sorting functions. Once we create them, we don't want to keep re-doing it
    // this takes an piece of data from the cell and tries to determine its type and what sorting
    // function to use for it
    // @item - the cell data
    sortService.guessSortFn = function(item) {
        var itemType = typeof(item);
        //check for numbers and booleans
        switch (itemType) {
            case "number":
                return sortService.sortNumber;
            case "boolean":
                return sortService.sortBool;
            case "string":
                // if number string return number string sort fn. else return the str
                return item.match(/^[-+]?[£$¤]?[\d,.]+%?$/) ? sortService.sortNumberStr : sortService.sortAlpha;
            default:
                //check if the item is a valid Date
                if (Object.prototype.toString.call(item) === '[object Date]') {
                    return sortService.sortDate;
                }
                else {
                    //finally just sort the basic sort...
                    return sortService.basicSort;
                }
        }
    };
    //#region Sorting Functions
    sortService.basicSort = function(a, b) {
        if (a === b) {
            return 0;
        }
        if (a < b) {
            return -1;
        }
        return 1;
    };
    sortService.sortNumber = function(a, b) {
        return a - b;
    };
    sortService.sortNumberStr = function(a, b) {
        var numA, numB, badA = false, badB = false;
        numA = parseFloat(a.replace(/[^0-9.-]/g, ''));
        if (isNaN(numA)) {
            badA = true;
        }
        numB = parseFloat(b.replace(/[^0-9.-]/g, ''));
        if (isNaN(numB)) {
            badB = true;
        }
        // we want bad ones to get pushed to the bottom... which effectively is "greater than"
        if (badA && badB) {
            return 0;
        }
        if (badA) {
            return 1;
        }
        if (badB) {
            return -1;
        }
        return numA - numB;
    };
    sortService.sortAlpha = function(a, b) {
        var strA = a.toLowerCase(),
            strB = b.toLowerCase();
        return strA === strB ? 0 : (strA < strB ? -1 : 1);
    };
    sortService.sortDate = function(a, b) {
        var timeA = a.getTime(),
            timeB = b.getTime();
        return timeA === timeB ? 0 : (timeA < timeB ? -1 : 1);
    };
    sortService.sortBool = function(a, b) {
        if (a && b) {
            return 0;
        }
        if (!a && !b) {
            return 0;
        } else {
            return a ? 1 : -1;
        }
    };
    //#endregion
    // the core sorting logic trigger
    sortService.sortData = function(sortInfo, data /*datasource*/) {
        // first make sure we are even supposed to do work
        if (!data || !sortInfo) {
            return;
        }
        var l = sortInfo.fields.length,
            order = sortInfo.fields,
            col,
            direction,
            // IE9 HACK.... omg, I can't reference data array within the sort fn below. has to be a separate reference....!!!!
            d = data.slice(0);
        //now actually sort the data
        data.sort(function (itemA, itemB) {
            var tem = 0,
                indx = 0,
                sortFn;
            while (tem === 0 && indx < l) {
                // grab the metadata for the rest of the logic
                col = sortInfo.columns[indx];
                direction = sortInfo.directions[indx];
                sortFn = sortService.getSortFn(col, d);
                
                var propA = $parse(order[indx])(itemA);
                var propB = $parse(order[indx])(itemB);
                // we want to allow zero values to be evaluated in the sort function
                if ((!propA && propA !== 0) || (!propB && propB !== 0)) {
                    // we want to force nulls and such to the bottom when we sort... which effectively is "greater than"
                    if (!propB && !propA) {
                        tem = 0;
                    }
                    else if (!propA) {
                        tem = 1;
                    }
                    else if (!propB) {
                        tem = -1;
                    }
                }
                else {
                    tem = sortFn(propA, propB);
                }
                indx++;
            }
            //made it this far, we don't have to worry about null & undefined
            if (direction === ASC) {
                return tem;
            } else {
                return 0 - tem;
            }
        });
    };
    sortService.Sort = function(sortInfo, data) {
        if (sortService.isSorting) {
            return;
        }
        sortService.isSorting = true;
        sortService.sortData(sortInfo, data);
        sortService.isSorting = false;
    };
    sortService.getSortFn = function(col, data) {
        var sortFn, item;
        //see if we already figured out what to use to sort the column
        if (sortService.colSortFnCache[col.field]) {
            sortFn = sortService.colSortFnCache[col.field];
        }
        else if (col.sortingAlgorithm !== undefined) {
            sortFn = col.sortingAlgorithm;
            sortService.colSortFnCache[col.field] = col.sortingAlgorithm;
        }
        else { // try and guess what sort function to use
            item = data[0];
            if (!item) {
                return sortFn;
            }
            sortFn = sortService.guessSortFn($parse(col.field)(item));
            //cache it
            if (sortFn) {
                sortService.colSortFnCache[col.field] = sortFn;
            } else {
                // we assign the alpha sort because anything that is null/undefined will never get passed to
                // the actual sorting function. It will get caught in our null check and returned to be sorted
                // down to the bottom
                sortFn = sortService.sortAlpha;
            }
        }
        return sortFn;
    };
    return sortService;
}]);

angular.module('ngGrid.services').factory('$utilityService', ['$parse', function ($parse) {
    var funcNameRegex = /function (.{1,})\(/;
    var utils = {
        visualLength: function(node) {
            var elem = document.getElementById('testDataLength');
            if (!elem) {
                elem = document.createElement('SPAN');
                elem.id = "testDataLength";
                elem.style.visibility = "hidden";
                document.body.appendChild(elem);
            }
            $(elem).css('font', $(node).css('font'));
            $(elem).css('font-size', $(node).css('font-size'));
            $(elem).css('font-family', $(node).css('font-family'));
            elem.innerHTML = $(node).text();
            return elem.offsetWidth;
        },
        forIn: function(obj, action) {
            for (var prop in obj) {
                if (obj.hasOwnProperty(prop)) {
                    action(obj[prop], prop);
                }
            }
        },
        evalProperty: function (entity, path) {
            return $parse(path)(entity);
        },
        endsWith: function(str, suffix) {
            if (!str || !suffix || typeof str !== "string") {
                return false;
            }
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
        },
        isNullOrUndefined: function(obj) {
            if (obj === undefined || obj === null) {
                return true;
            }
            return false;
        },
        getElementsByClassName: function(cl) {
            var retnode = [];
            var myclass = new RegExp('\\b' + cl + '\\b');
            var elem = document.getElementsByTagName('*');
            for (var i = 0; i < elem.length; i++) {
                var classes = elem[i].className;
                if (myclass.test(classes)) {
                    retnode.push(elem[i]);
                }
            }
            return retnode;
        },
        newId: (function() {
            var seedId = new Date().getTime();
            return function() {
                return seedId += 1;
            };
        })(),
        seti18n: function($scope, language) {
            var $langPack = window.ngGrid.i18n[language];
            for (var label in $langPack) {
                $scope.i18n[label] = $langPack[label];
            }
        },
        getInstanceType: function (o) {
            var results = (funcNameRegex).exec(o.constructor.toString());
            if (results && results.length > 1) {
                var instanceType = results[1].replace(/^\s+|\s+$/g, ""); // Trim surrounding whitespace; IE appears to add a space at the end
                return instanceType;
            }
            else {
                return "";
            }
        },
        // Detect IE versions for bug workarounds (uses IE conditionals, not UA string, for robustness)
        // Note that, since IE 10 does not support conditional comments, the following logic only detects IE < 10.
        // Currently this is by design, since IE 10+ behaves correctly when treated as a standard browser.
        // If there is a future need to detect specific versions of IE10+, we will amend this.
        ieVersion: (function() {
            var version = 3, div = document.createElement('div'), iElems = div.getElementsByTagName('i');

            // Keep constructing conditional HTML blocks until we hit one that resolves to an empty fragment
            do{
                div.innerHTML = '<!--[if gt IE ' + (++version) + ']><i></i><![endif]-->';
            }while(iElems[0]);
            return version > 4 ? version : undefined;
        })()
    };

    $.extend(utils, {
        isIe: (function() {
            return utils.ieVersion !== undefined;
        })()
    });
    return utils;
}]);

var ngAggregate = function (aggEntity, rowFactory, rowHeight, groupInitState) {
    this.rowIndex = 0;
    this.offsetTop = this.rowIndex * rowHeight;
    this.entity = aggEntity;
    this.label = aggEntity.gLabel;
    this.field = aggEntity.gField;
    this.depth = aggEntity.gDepth;
    this.parent = aggEntity.parent;
    this.children = aggEntity.children;
    this.aggChildren = aggEntity.aggChildren;
    this.aggIndex = aggEntity.aggIndex;
    this.collapsed = groupInitState;
    this.groupInitState = groupInitState;
    this.rowFactory = rowFactory;
    this.rowHeight = rowHeight;
    this.isAggRow = true;
    this.offsetLeft = aggEntity.gDepth * 25;
    this.aggLabelFilter = aggEntity.aggLabelFilter;
};

ngAggregate.prototype.toggleExpand = function () {
    this.collapsed = this.collapsed ? false : true;
    if (this.orig) {
        this.orig.collapsed = this.collapsed;
    }
    this.notifyChildren();
};
ngAggregate.prototype.setExpand = function (state) {
    this.collapsed = state;
    this.notifyChildren();
};
ngAggregate.prototype.notifyChildren = function () {
    var longest = Math.max(this.rowFactory.aggCache.length, this.children.length);
    for (var i = 0; i < longest; i++) {
        if (this.aggChildren[i]) {
            this.aggChildren[i].entity[NG_HIDDEN] = this.collapsed;
            if (this.collapsed) {
                this.aggChildren[i].setExpand(this.collapsed);
            }
        }
        if (this.children[i]) {
            this.children[i][NG_HIDDEN] = this.collapsed;
        }
        if (i > this.aggIndex && this.rowFactory.aggCache[i]) {
            var agg = this.rowFactory.aggCache[i];
            var offset = (30 * this.children.length);
            agg.offsetTop = this.collapsed ? agg.offsetTop - offset : agg.offsetTop + offset;
        }
    }
    this.rowFactory.renderedChange();
};
ngAggregate.prototype.aggClass = function () {
    return this.collapsed ? "ngAggArrowCollapsed" : "ngAggArrowExpanded";
};
ngAggregate.prototype.totalChildren = function () {
    if (this.aggChildren.length > 0) {
        var i = 0;
        var recurse = function (cur) {
            if (cur.aggChildren.length > 0) {
                angular.forEach(cur.aggChildren, function (a) {
                    recurse(a);
                });
            } else {
                i += cur.children.length;
            }
        };
        recurse(this);
        return i;
    } else {
        return this.children.length;
    }
};
ngAggregate.prototype.copy = function () {
    var ret = new ngAggregate(this.entity, this.rowFactory, this.rowHeight, this.groupInitState);
    ret.orig = this;
    return ret;
};
var ngColumn = function (config, $scope, grid, domUtilityService, $templateCache, $utils) {
    var self = this,
        colDef = config.colDef,
        delay = 500,
        clicks = 0,
        timer = null;
    self.colDef = config.colDef;
    self.width = colDef.width;
    self.groupIndex = 0;
    self.isGroupedBy = false;
    self.minWidth = !colDef.minWidth ? 50 : colDef.minWidth;
    self.maxWidth = !colDef.maxWidth ? 9000 : colDef.maxWidth;

    // TODO: Use the column's definition for enabling cell editing
    // self.enableCellEdit = config.enableCellEdit || colDef.enableCellEdit;
    self.enableCellEdit = colDef.enableCellEdit !== undefined ? colDef.enableCellEdit : (config.enableCellEdit || config.enableCellEditOnFocus);

    self.headerRowHeight = config.headerRowHeight;

    // Use colDef.displayName as long as it's not undefined, otherwise default to the field name
    self.displayName = (colDef.displayName === undefined) ? colDef.field : colDef.displayName;

    self.index = config.index;
    self.isAggCol = config.isAggCol;
    self.cellClass = colDef.cellClass;
    self.sortPriority = undefined;
    self.cellFilter = colDef.cellFilter ? colDef.cellFilter : "";
    self.field = colDef.field;
    self.aggLabelFilter = colDef.cellFilter || colDef.aggLabelFilter;
    self.visible = $utils.isNullOrUndefined(colDef.visible) || colDef.visible;
    self.sortable = false;
    self.resizable = false;
    self.pinnable = false;
    self.pinned = (config.enablePinning && colDef.pinned);
    self.originalIndex = config.originalIndex == null ? self.index : config.originalIndex;
    self.groupable = $utils.isNullOrUndefined(colDef.groupable) || colDef.groupable;
    if (config.enableSort) {
        self.sortable = $utils.isNullOrUndefined(colDef.sortable) || colDef.sortable;
    }
    if (config.enableResize) {
        self.resizable = $utils.isNullOrUndefined(colDef.resizable) || colDef.resizable;
    }
    if (config.enablePinning) {
        self.pinnable = $utils.isNullOrUndefined(colDef.pinnable) || colDef.pinnable;
    }
    self.sortDirection = undefined;
    self.sortingAlgorithm = colDef.sortFn;
    self.headerClass = colDef.headerClass;
    self.cursor = self.sortable ? 'pointer' : 'default';
    self.headerCellTemplate = colDef.headerCellTemplate || $templateCache.get('headerCellTemplate.html');
    self.cellTemplate = colDef.cellTemplate || $templateCache.get('cellTemplate.html').replace(CUSTOM_FILTERS, self.cellFilter ? "|" + self.cellFilter : "");
    if(self.enableCellEdit) {
        self.cellEditTemplate = $templateCache.get('cellEditTemplate.html');
        self.editableCellTemplate = colDef.editableCellTemplate || $templateCache.get('editableCellTemplate.html');
    }
    if (colDef.cellTemplate && !TEMPLATE_REGEXP.test(colDef.cellTemplate)) {
        self.cellTemplate = $.ajax({
            type: "GET",
            url: colDef.cellTemplate,
            async: false
        }).responseText;
    }
    if (self.enableCellEdit && colDef.editableCellTemplate && !TEMPLATE_REGEXP.test(colDef.editableCellTemplate)) {
        self.editableCellTemplate = $.ajax({
            type: "GET",
            url: colDef.editableCellTemplate,
            async: false
        }).responseText;
    }
    if (colDef.headerCellTemplate && !TEMPLATE_REGEXP.test(colDef.headerCellTemplate)) {
        self.headerCellTemplate = $.ajax({
            type: "GET",
            url: colDef.headerCellTemplate,
            async: false
        }).responseText;
    }
    self.colIndex = function () {
        var classes = self.pinned ? "pinned " : "";
        classes += "col" + self.index + " colt" + self.index;
        if (self.cellClass) {
            classes += " " + self.cellClass;
        }
        return classes;
    };
    self.groupedByClass = function() {
        return self.isGroupedBy ? "ngGroupedByIcon" : "ngGroupIcon";
    };
    self.toggleVisible = function() {
        self.visible = !self.visible;
    };
    self.showSortButtonUp = function() {
        return self.sortable ? self.sortDirection === DESC : self.sortable;
    };
    self.showSortButtonDown = function() {
        return self.sortable ? self.sortDirection === ASC : self.sortable;
    };
    self.noSortVisible = function() {
        return !self.sortDirection;
    };
    self.sort = function(evt) {
        if (!self.sortable) {
            return true; // column sorting is disabled, do nothing
        }
        var dir = self.sortDirection === ASC ? DESC : ASC;
        self.sortDirection = dir;
        config.sortCallback(self, evt);
        return false;
    };
    self.gripClick = function() {
        clicks++; //count clicks
        if (clicks === 1) {
            timer = setTimeout(function() {
                //Here you can add a single click action.
                clicks = 0; //after action performed, reset counter
            }, delay);
        } else {
            clearTimeout(timer); //prevent single-click action
            config.resizeOnDataCallback(self); //perform double-click action
            clicks = 0; //after action performed, reset counter
        }
    };
    self.gripOnMouseDown = function(event) {
        $scope.isColumnResizing = true;
        if (event.ctrlKey && !self.pinned) {
            self.toggleVisible();
            domUtilityService.BuildStyles($scope, grid);
            return true;
        }
        event.target.parentElement.style.cursor = 'col-resize';
        self.startMousePosition = event.clientX;
        self.origWidth = self.width;
        $(document).mousemove(self.onMouseMove);
        $(document).mouseup(self.gripOnMouseUp);
        return false;
    };
    self.onMouseMove = function(event) {
        var diff = event.clientX - self.startMousePosition;
        var newWidth = diff + self.origWidth;
        self.width = (newWidth < self.minWidth ? self.minWidth : (newWidth > self.maxWidth ? self.maxWidth : newWidth));
        $scope.hasUserChangedGridColumnWidths = true;
        domUtilityService.BuildStyles($scope, grid);
        return false;
    };
    self.gripOnMouseUp = function (event) {
        $(document).off('mousemove', self.onMouseMove);
        $(document).off('mouseup', self.gripOnMouseUp);
        event.target.parentElement.style.cursor = 'default';
        domUtilityService.digest($scope);
        $scope.isColumnResizing = false;
        return false;
    };
    self.copy = function() {
        var ret = new ngColumn(config, $scope, grid, domUtilityService, $templateCache);
        ret.isClone = true;
        ret.orig = self;
        return ret;
    };
    self.setVars = function (fromCol) {
        self.orig = fromCol;
        self.width = fromCol.width;
        self.groupIndex = fromCol.groupIndex;
        self.isGroupedBy = fromCol.isGroupedBy;
        self.displayName = fromCol.displayName;
        self.index = fromCol.index;
        self.isAggCol = fromCol.isAggCol;
        self.cellClass = fromCol.cellClass;
        self.cellFilter = fromCol.cellFilter;
        self.field = fromCol.field;
        self.aggLabelFilter = fromCol.aggLabelFilter;
        self.visible = fromCol.visible;
        self.sortable = fromCol.sortable;
        self.resizable = fromCol.resizable;
        self.pinnable = fromCol.pinnable;
        self.pinned = fromCol.pinned;
        self.originalIndex = fromCol.originalIndex;
        self.sortDirection = fromCol.sortDirection;
        self.sortingAlgorithm = fromCol.sortingAlgorithm;
        self.headerClass = fromCol.headerClass;
        self.headerCellTemplate = fromCol.headerCellTemplate;
        self.cellTemplate = fromCol.cellTemplate;
        self.cellEditTemplate = fromCol.cellEditTemplate;
    };
};

var ngDimension = function (options) {
    this.outerHeight = null;
    this.outerWidth = null;
    $.extend(this, options);
};
var ngDomAccessProvider = function (grid) {
    this.previousColumn = null;
    this.grid = grid;

};

ngDomAccessProvider.prototype.changeUserSelect = function (elm, value) {
    elm.css({
        '-webkit-touch-callout': value,
        '-webkit-user-select': value,
        '-khtml-user-select': value,
        '-moz-user-select': value === 'none' ? '-moz-none' : value,
        '-ms-user-select': value,
        'user-select': value
    });
};
ngDomAccessProvider.prototype.focusCellElement = function ($scope, index) { 
    if ($scope.selectionProvider.lastClickedRow) {
        var columnIndex = index !== undefined ? index : this.previousColumn;
        var elm = $scope.selectionProvider.lastClickedRow.clone ? $scope.selectionProvider.lastClickedRow.clone.elm : $scope.selectionProvider.lastClickedRow.elm;
        if (columnIndex !== undefined && elm) {
            var columns = angular.element(elm[0].children).filter(function () { return this.nodeType !== 8; }); //Remove html comments for IE8
            var i = Math.max(Math.min($scope.renderedColumns.length - 1, columnIndex), 0);
            if (this.grid.config.showSelectionCheckbox && angular.element(columns[i]).scope() && angular.element(columns[i]).scope().col.index === 0) {
                i = 1; //don't want to focus on checkbox
            }
            if (columns[i]) {
                columns[i].children[1].children[0].focus();
            }
            this.previousColumn = columnIndex;
        }
    }
};
ngDomAccessProvider.prototype.selectionHandlers = function ($scope, elm) {
    var doingKeyDown = false;
    var self = this;
    elm.bind('keydown', function (evt) {
        if (evt.keyCode === 16) { //shift key
            self.changeUserSelect(elm, 'none', evt);
            return true;
        } else if (!doingKeyDown) {
            doingKeyDown = true;
            var ret = ngMoveSelectionHandler($scope, elm, evt, self.grid);
            doingKeyDown = false;
            return ret;
        }
        return true;
    });
    elm.bind('keyup', function (evt) {
        if (evt.keyCode === 16) { //shift key
            self.changeUserSelect(elm, 'text', evt);
        }
        return true;
    });
};
var ngEventProvider = function (grid, $scope, domUtilityService, $timeout) {
    var self = this;
    // The init method gets called during the ng-grid directive execution.
    self.colToMove = undefined;
    self.groupToMove = undefined;
    self.assignEvents = function() {
        // Here we set the onmousedown event handler to the header container.
        if (grid.config.jqueryUIDraggable && !grid.config.enablePinning) {
            grid.$groupPanel.droppable({
                addClasses: false,
                drop: function(event) {
                    self.onGroupDrop(event);
                }
            });
        } else {
            grid.$groupPanel.on('mousedown', self.onGroupMouseDown).on('dragover', self.dragOver).on('drop', self.onGroupDrop);
            grid.$headerScroller.on('mousedown', self.onHeaderMouseDown).on('dragover', self.dragOver);
            if (grid.config.enableColumnReordering && !grid.config.enablePinning) {
                grid.$headerScroller.on('drop', self.onHeaderDrop);
            }
        }
        $scope.$watch('renderedColumns', function() {
            $timeout(self.setDraggables);
        });
    };
    self.dragStart = function(evt){		
      //FireFox requires there to be dataTransfer if you want to drag and drop.
      evt.dataTransfer.setData('text', ''); //cannot be empty string
    };
    self.dragOver = function(evt) {
        evt.preventDefault();
    };
    //For JQueryUI
    self.setDraggables = function() {
        if (!grid.config.jqueryUIDraggable) {
            //Fix for FireFox. Instead of using jQuery on('dragstart', function) on find, we have to use addEventListeners for each column.
            var columns = grid.$root.find('.ngHeaderSortColumn'); //have to iterate if using addEventListener
            angular.forEach(columns, function(col){
                if(col.className && col.className.indexOf("ngHeaderSortColumn") !== -1){
                    col.setAttribute('draggable', 'true');
                    //jQuery 'on' function doesn't have  dataTransfer as part of event in handler unless added to event props, which is not recommended
                    //See more here: http://api.jquery.com/category/events/event-object/
                    if (col.addEventListener) { //IE8 doesn't have drag drop or event listeners
                        col.addEventListener('dragstart', self.dragStart);
                    }
                }
            });
            if (navigator.userAgent.indexOf("MSIE") !== -1){
                //call native IE dragDrop() to start dragging
                grid.$root.find('.ngHeaderSortColumn').bind('selectstart', function () { 
                    this.dragDrop(); 
                    return false; 
                });	
            }
        } else {
            grid.$root.find('.ngHeaderSortColumn').draggable({
                helper: 'clone',
                appendTo: 'body',
                stack: 'div',
                addClasses: false,
                start: function(event) {
                    self.onHeaderMouseDown(event);
                }
            }).droppable({
                drop: function(event) {
                    self.onHeaderDrop(event);
                }
            });
        }
    };
    self.onGroupMouseDown = function(event) {
        var groupItem = $(event.target);
        // Get the scope from the header container
        if (groupItem[0].className !== 'ngRemoveGroup') {
            var groupItemScope = angular.element(groupItem).scope();
            if (groupItemScope) {
                // set draggable events
                if (!grid.config.jqueryUIDraggable) {
                    groupItem.attr('draggable', 'true');
                    if(this.addEventListener){//IE8 doesn't have drag drop or event listeners
                        this.addEventListener('dragstart', self.dragStart); 
                    }
                    if (navigator.userAgent.indexOf("MSIE") !== -1){
                        //call native IE dragDrop() to start dragging
                        groupItem.bind('selectstart', function () { 
                            this.dragDrop(); 
                            return false; 
                        });	
                    }
                }
                // Save the column for later.
                self.groupToMove = { header: groupItem, groupName: groupItemScope.group, index: groupItemScope.$index };
            }
        } else {
            self.groupToMove = undefined;
        }
    };
    self.onGroupDrop = function(event) {
        event.stopPropagation();
        // clear out the colToMove object
        var groupContainer;
        var groupScope;
        if (self.groupToMove) {
            // Get the closest header to where we dropped
            groupContainer = $(event.target).closest('.ngGroupElement'); // Get the scope from the header.
            if (groupContainer.context.className === 'ngGroupPanel') {
                $scope.configGroups.splice(self.groupToMove.index, 1);
                $scope.configGroups.push(self.groupToMove.groupName);
            } else {
                groupScope = angular.element(groupContainer).scope();
                if (groupScope) {
                    // If we have the same column, do nothing.
                    if (self.groupToMove.index !== groupScope.$index) {
                        // Splice the columns
                        $scope.configGroups.splice(self.groupToMove.index, 1);
                        $scope.configGroups.splice(groupScope.$index, 0, self.groupToMove.groupName);
                    }
                }
            }
            self.groupToMove = undefined;
            grid.fixGroupIndexes();
        } else if (self.colToMove) {
            if ($scope.configGroups.indexOf(self.colToMove.col) === -1) {
                groupContainer = $(event.target).closest('.ngGroupElement'); // Get the scope from the header.
                if (groupContainer.context.className === 'ngGroupPanel' || groupContainer.context.className === 'ngGroupPanelDescription ng-binding') {
                    $scope.groupBy(self.colToMove.col);
                } else {
                    groupScope = angular.element(groupContainer).scope();
                    if (groupScope) {
                        // Splice the columns
                        $scope.removeGroup(groupScope.$index);
                    }
                }
            }
            self.colToMove = undefined;
        }
        if (!$scope.$$phase) {
            $scope.$apply();
        }
    };
    //Header functions
    self.onHeaderMouseDown = function(event) {
        // Get the closest header container from where we clicked.
        var headerContainer = $(event.target).closest('.ngHeaderSortColumn');
        // Get the scope from the header container
        var headerScope = angular.element(headerContainer).scope();
        if (headerScope) {
            // Save the column for later.
            self.colToMove = { header: headerContainer, col: headerScope.col };
        }
    };
    self.onHeaderDrop = function(event) {
        if (!self.colToMove || self.colToMove.col.pinned) {
            return;
        }
        // Get the closest header to where we dropped
        var headerContainer = $(event.target).closest('.ngHeaderSortColumn');
        // Get the scope from the header.
        var headerScope = angular.element(headerContainer).scope();
        if (headerScope) {
            // If we have the same column, do nothing.
            if (self.colToMove.col === headerScope.col) {
                return;
            }
            // Splice the columns
            $scope.columns.splice(self.colToMove.col.index, 1);
            $scope.columns.splice(headerScope.col.index, 0, self.colToMove.col);
            grid.fixColumnIndexes();
            // clear out the colToMove object
            self.colToMove = undefined;
            domUtilityService.digest($scope);
        }
    };

    self.assignGridEventHandlers = function() {
        //Chrome and firefox both need a tab index so the grid can recieve focus.
        //need to give the grid a tabindex if it doesn't already have one so
        //we'll just give it a tab index of the corresponding gridcache index 
        //that way we'll get the same result every time it is run.
        //configurable within the options.
        if (grid.config.tabIndex === -1) {
            grid.$viewport.attr('tabIndex', domUtilityService.numberOfGrids);
            domUtilityService.numberOfGrids++;
        } else {
            grid.$viewport.attr('tabIndex', grid.config.tabIndex);
        }
        // resize on window resize
        var windowThrottle;
        $(window).resize(function(){
            clearTimeout(windowThrottle);
            windowThrottle = setTimeout(function() {
                //in function for IE8 compatibility
                domUtilityService.RebuildGrid($scope,grid);
            }, 100);
        });
        // resize on parent resize as well.
        var parentThrottle;
        $(grid.$root.parent()).on('resize', function() {
            clearTimeout(parentThrottle);
            parentThrottle = setTimeout(function() {
                //in function for IE8 compatibility
                domUtilityService.RebuildGrid($scope,grid);
            }, 100);
        });
    };
    // In this example we want to assign grid events.
    self.assignGridEventHandlers();
    self.assignEvents();
};

var ngFooter = function ($scope, grid) {
    $scope.maxRows = function () {
        var ret = Math.max($scope.totalServerItems, grid.data.length);
        return ret;
    };
    
    $scope.multiSelect = (grid.config.enableRowSelection && grid.config.multiSelect);
    $scope.selectedItemCount = grid.selectedItemCount;
    $scope.maxPages = function () {
        return Math.ceil($scope.maxRows() / $scope.pagingOptions.pageSize);
    };

    $scope.pageForward = function() {
        var page = $scope.pagingOptions.currentPage;
        if ($scope.totalServerItems > 0) {
            $scope.pagingOptions.currentPage = Math.min(page + 1, $scope.maxPages());
        } else {
            $scope.pagingOptions.currentPage++;
        }
    };

    $scope.pageBackward = function() {
        var page = $scope.pagingOptions.currentPage;
        $scope.pagingOptions.currentPage = Math.max(page - 1, 1);
    };

    $scope.pageToFirst = function() {
        $scope.pagingOptions.currentPage = 1;
    };

    $scope.pageToLast = function() {
        var maxPages = $scope.maxPages();
        $scope.pagingOptions.currentPage = maxPages;
    };

    $scope.cantPageForward = function() {
        var curPage = $scope.pagingOptions.currentPage;
        var maxPages = $scope.maxPages();
        if ($scope.totalServerItems > 0) {
            return curPage >= maxPages;
        } else {
            return grid.data.length < 1;
        }

    };
    $scope.cantPageToLast = function() {
        if ($scope.totalServerItems > 0) {
            return $scope.cantPageForward();
        } else {
            return true;
        }
    };
    
    $scope.cantPageBackward = function() {
        var curPage = $scope.pagingOptions.currentPage;
        return curPage <= 1;
    };
};
/// <reference path="footer.js" />
/// <reference path="../services/SortService.js" />
/// <reference path="../../lib/jquery-1.8.2.min" />
var ngGrid = function ($scope, options, sortService, domUtilityService, $filter, $templateCache, $utils, $timeout, $parse, $http, $q) {
    var defaults = {
        //Define an aggregate template to customize the rows when grouped. See github wiki for more details.
        aggregateTemplate: undefined,
        
        //Callback for when you want to validate something after selection.
        afterSelectionChange: function() {
        }, 

        /* Callback if you want to inspect something before selection,
        return false if you want to cancel the selection. return true otherwise. 
        If you need to wait for an async call to proceed with selection you can 
        use rowItem.changeSelection(event) method after returning false initially. 
        Note: when shift+ Selecting multiple items in the grid this will only get called
        once and the rowItem will be an array of items that are queued to be selected. */
        beforeSelectionChange: function() {
            return true;
        },

        //checkbox templates.
        checkboxCellTemplate: undefined,
        checkboxHeaderTemplate: undefined,
        
        //definitions of columns as an array [], if not defines columns are auto-generated. See github wiki for more details.
        columnDefs: undefined,

        //*Data being displayed in the grid. Each item in the array is mapped to a row being displayed.
        data: [],
        
        //Data updated callback, fires every time the data is modified from outside the grid.
        dataUpdated: function() {
        },

        //Enables cell editing.
        enableCellEdit: false,

        //Enables cell editing on focus
        enableCellEditOnFocus: false,
        
        //Enables cell selection.
        enableCellSelection: false,

        //Enable or disable resizing of columns
        enableColumnResize: false,

        //Enable or disable reordering of columns
        enableColumnReordering: false,

        //Enable or disable HEAVY column virtualization. This turns off selection checkboxes and column pinning and is designed for spreadsheet-like data.
        enableColumnHeavyVirt: false,

        //Enables the server-side paging feature
        enablePaging: false,

        //Enable column pinning
        enablePinning: false,
        
        //To be able to have selectable rows in grid.
        enableRowSelection: true,

        //Enables or disables sorting in grid.
        enableSorting: true,

        //Enables or disables text highlighting in grid by adding the "unselectable" class (See CSS file)
        enableHighlighting: false,
        
        // string list of properties to exclude when auto-generating columns.
        excludeProperties: [],
        
        /* filterOptions -
        filterText: The text bound to the built-in search box. 
        useExternalFilter: Bypass internal filtering if you want to roll your own filtering mechanism but want to use builtin search box.
        */
        filterOptions: {
            filterText: "",
            useExternalFilter: false
        },

        //Defining the height of the footer in pixels.
        footerRowHeight: 55,
        
        // the template for the column menu and filter, including the button.
        footerTemplate: undefined,

        //Initial fields to group data by. Array of field names, not displayName.
        groups: [],
        
        // set the initial state of aggreagate grouping. "true" means they will be collapsed when grouping changes, "false" means they will be expanded by default.
        groupsCollapsedByDefault: true,
        
        //The height of the header row in pixels.
        headerRowHeight: 30,

        //Define a header row template for further customization. See github wiki for more details.
        headerRowTemplate: undefined,

        /*Enables the use of jquery UI reaggable/droppable plugin. requires jqueryUI to work if enabled. 
        Useful if you want drag + drop but your users insist on crappy browsers. */
        jqueryUIDraggable: false,

        //Enable the use jqueryUIThemes
        jqueryUITheme: false,

        //Prevent unselections when in single selection mode.
        keepLastSelected: true,

        /*Maintains the column widths while resizing. 
        Defaults to true when using *'s or undefined widths. Can be ovverriden by setting to false.*/
        maintainColumnRatios: undefined,

        // the template for the column menu and filter, including the button.
        menuTemplate: undefined,

        //Set this to false if you only want one item selected at a time
        multiSelect: true,

        // pagingOptions -
        pagingOptions: {
            // pageSizes: list of available page sizes.
            pageSizes: [250, 500, 1000], 
            //pageSize: currently selected page size. 
            pageSize: 250,
            //currentPage: the uhm... current page.
            currentPage: 1
        },
        
        //the selection checkbox is pinned to the left side of the viewport or not.
        pinSelectionCheckbox: false,

        //Array of plugin functions to register in ng-grid
        plugins: [],

        //User defined unique ID field that allows for better handling of selections and for server-side paging
        primaryKey: undefined,

        //Row height of rows in grid.
        rowHeight: 30,
        
        //Define a row template to customize output. See github wiki for more details.
        rowTemplate: undefined,
        
        //all of the items selected in the grid. In single select mode there will only be one item in the array.
        selectedItems: [],
        
        //Disable row selections by clicking on the row and only when the checkbox is clicked.
        selectWithCheckboxOnly: false,
        
        /*Enables menu to choose which columns to display and group by. 
        If both showColumnMenu and showFilter are false the menu button will not display.*/
        showColumnMenu: false,

        /*Enables display of the filterbox in the column menu. 
        If both showColumnMenu and showFilter are false the menu button will not display.*/
        showFilter: false,
        
        //Show or hide the footer alltogether the footer is enabled by default
        showFooter: false,

        //Show the dropzone for drag and drop grouping
        showGroupPanel: false,
        
        //Row selection check boxes appear as the first column.
        showSelectionCheckbox: false,
        
        /*Define a sortInfo object to specify a default sorting state. 
        You can also observe this variable to utilize server-side sorting (see useExternalSorting).
        Syntax is sortinfo: { fields: ['fieldName1',' fieldName2'], direction: 'ASC'/'asc' || 'desc'/'DESC'}*/
        sortInfo: {fields: [], columns: [], directions: [] },

        //Set the tab index of the Vieport.
        tabIndex: -1,
        
        //totalServerItems: Total items are on the server. 
        totalServerItems: 0,
            
        /*Prevents the internal sorting from executing. 
        The sortInfo object will be updated with the sorting information so you can handle sorting (see sortInfo)*/
        useExternalSorting: false,
        
        /*i18n language support. choose from the installed or included languages, en, fr, sp, etc...*/
        i18n: 'en',
        
        //the threshold in rows to force virtualization on
        virtualizationThreshold: 50
    },
        self = this;
    self.maxCanvasHt = 0;
    //self vars
    self.config = $.extend(defaults, window.ngGrid.config, options);

    // override conflicting settings
    self.config.showSelectionCheckbox = (self.config.showSelectionCheckbox && self.config.enableColumnHeavyVirt === false);
    self.config.enablePinning = (self.config.enablePinning && self.config.enableColumnHeavyVirt === false);
    self.config.selectWithCheckboxOnly = (self.config.selectWithCheckboxOnly && self.config.showSelectionCheckbox !== false);
    self.config.pinSelectionCheckbox = self.config.enablePinning;

    if (typeof options.columnDefs === "string") {
        self.config.columnDefs = $scope.$eval(options.columnDefs);
    }
    self.rowCache = [];
    self.rowMap = [];
    self.gridId = "ng" + $utils.newId();
    self.$root = null; //this is the root element that is passed in with the binding handler
    self.$groupPanel = null;
    self.$topPanel = null;
    self.$headerContainer = null;
    self.$headerScroller = null;
    self.$headers = null;
    self.$viewport = null;
    self.$canvas = null;
    self.rootDim = self.config.gridDim;
    self.data = [];
    self.lateBindColumns = false;
    self.filteredRows = [];

    self.initTemplates = function() {
        var templates = ['rowTemplate', 'aggregateTemplate', 'headerRowTemplate', 'checkboxCellTemplate', 'checkboxHeaderTemplate', 'menuTemplate', 'footerTemplate'];

        var promises = [];
        angular.forEach(templates, function(template) {
            promises.push( self.getTemplate(template) );
        });

        return $q.all(promises);
    };
    
    //Templates
    // test templates for urls and get the tempaltes via synchronous ajax calls
    self.getTemplate = function (key) {
        var t = self.config[key];
        var uKey = self.gridId + key + ".html";
        var p = $q.defer();
        if (t && !TEMPLATE_REGEXP.test(t)) {
            $http.get(t, {
                cache: $templateCache
            })
            .success(function(data){
                $templateCache.put(uKey, data);
                p.resolve();
            })
            .error(function(err){
                p.reject("Could not load template: " + t);
            });
        } else if (t) {
            $templateCache.put(uKey, t);
            p.resolve();
        } else {
            var dKey = key + ".html";
            $templateCache.put(uKey, $templateCache.get(dKey));
            p.resolve();
        }

        return p.promise;
    };

    if (typeof self.config.data === "object") {
        self.data = self.config.data; // we cannot watch for updates if you don't pass the string name
    }
    self.calcMaxCanvasHeight = function() {
        var calculatedHeight;
        if(self.config.groups.length > 0){
            calculatedHeight = self.rowFactory.parsedData.filter(function(e) {
                return !e[NG_HIDDEN];
            }).length * self.config.rowHeight;
        } else {
            calculatedHeight = self.filteredRows.length * self.config.rowHeight;
        }
        return calculatedHeight;
    };
    self.elementDims = {
        scrollW: 0,
        scrollH: 0,
        rowIndexCellW: 25,
        rowSelectedCellW: 25,
        rootMaxW: 0,
        rootMaxH: 0
    };
    //self funcs
    self.setRenderedRows = function (newRows) {
        $scope.renderedRows.length = newRows.length;
        for (var i = 0; i < newRows.length; i++) {
            if (!$scope.renderedRows[i] || (newRows[i].isAggRow || $scope.renderedRows[i].isAggRow)) {
                $scope.renderedRows[i] = newRows[i].copy();
                $scope.renderedRows[i].collapsed = newRows[i].collapsed;
                if (!newRows[i].isAggRow) {
                    $scope.renderedRows[i].setVars(newRows[i]);
                }
            } else {
                $scope.renderedRows[i].setVars(newRows[i]);
            }
            $scope.renderedRows[i].rowIndex = newRows[i].rowIndex;
            $scope.renderedRows[i].offsetTop = newRows[i].offsetTop;
            $scope.renderedRows[i].selected = newRows[i].selected;
            newRows[i].renderedRowIndex = i;
        }
        self.refreshDomSizes();
        $scope.$emit('ngGridEventRows', newRows);
    };
    self.minRowsToRender = function() {
        var viewportH = $scope.viewportDimHeight() || 1;
        return Math.floor(viewportH / self.config.rowHeight);
    };
    self.refreshDomSizes = function() {
        var dim = new ngDimension();
        dim.outerWidth = self.elementDims.rootMaxW;
        dim.outerHeight = self.elementDims.rootMaxH;
        self.rootDim = dim;
        self.maxCanvasHt = self.calcMaxCanvasHeight();
    };
    self.buildColumnDefsFromData = function () {
        self.config.columnDefs = [];
        var item = self.data[0];
        if (!item) {
            self.lateBoundColumns = true;
            return;
        }
        $utils.forIn(item, function (prop, propName) {
            if (self.config.excludeProperties.indexOf(propName) === -1) {
                self.config.columnDefs.push({
                    field: propName
                });
            }
        });
    };
    self.buildColumns = function() {
        var columnDefs = self.config.columnDefs,
            cols = [];
        if (!columnDefs) {
            self.buildColumnDefsFromData();
            columnDefs = self.config.columnDefs;
        }
        if (self.config.showSelectionCheckbox) {
            cols.push(new ngColumn({
                colDef: {
                    field: '\u2714',
                    width: self.elementDims.rowSelectedCellW,
                    sortable: false,
                    resizable: false,
                    groupable: false,
                    headerCellTemplate: $templateCache.get($scope.gridId + 'checkboxHeaderTemplate.html'),
                    cellTemplate: $templateCache.get($scope.gridId + 'checkboxCellTemplate.html'),
                    pinned: self.config.pinSelectionCheckbox
                },
                index: 0,
                headerRowHeight: self.config.headerRowHeight,
                sortCallback: self.sortData,
                resizeOnDataCallback: self.resizeOnData,
                enableResize: self.config.enableColumnResize,
                enableSort: self.config.enableSorting,
                enablePinning: self.config.enablePinning
            }, $scope, self, domUtilityService, $templateCache, $utils));
        }
        if (columnDefs.length > 0) {
            var checkboxOffset = self.config.showSelectionCheckbox ? 1 : 0;
            var groupOffset = $scope.configGroups.length;
            $scope.configGroups.length = 0;
            angular.forEach(columnDefs, function(colDef, i) {
                i += checkboxOffset;
                var column = new ngColumn({
                    colDef: colDef,
                    index: i + groupOffset,
                    originalIndex: i,
                    headerRowHeight: self.config.headerRowHeight,
                    sortCallback: self.sortData,
                    resizeOnDataCallback: self.resizeOnData,
                    enableResize: self.config.enableColumnResize,
                    enableSort: self.config.enableSorting,
                    enablePinning: self.config.enablePinning,
                    enableCellEdit: self.config.enableCellEdit || self.config.enableCellEditOnFocus
                }, $scope, self, domUtilityService, $templateCache, $utils);
                var indx = self.config.groups.indexOf(colDef.field);
                if (indx !== -1) {
                    column.isGroupedBy = true;
                    $scope.configGroups.splice(indx, 0, column);
                    column.groupIndex = $scope.configGroups.length;
                }
                cols.push(column);
            });
            $scope.columns = cols;
            if (self.config.groups.length > 0) {
                self.rowFactory.getGrouping(self.config.groups);
            }
        }
    };
    self.configureColumnWidths = function() {
        var asterisksArray = [],
            percentArray = [],
            asteriskNum = 0,
            totalWidth = 0;

        // When rearranging columns, their index in $scope.columns will no longer match the original column order from columnDefs causing
        // their width config to be out of sync. We can use "originalIndex" on the ngColumns to get hold of the correct setup from columnDefs, but to
        // avoid O(n) lookups in $scope.columns per column we setup a map.
        var indexMap = {};
        // Build a map of columnDefs column indices -> ngColumn indices (via the "originalIndex" property on ngColumns).
        angular.forEach($scope.columns, function(ngCol, i) {
            // Disregard columns created by grouping (the grouping columns don't match a column from columnDefs)
            if (!$utils.isNullOrUndefined(ngCol.originalIndex)) {
                var origIndex = ngCol.originalIndex;
                if (self.config.showSelectionCheckbox) {
                    //if visible, takes up 25 pixels
                    if(ngCol.originalIndex === 0 && ngCol.visible){
                        totalWidth += 25;
                    }
                    // The originalIndex will be offset 1 when including the selection column
                    origIndex--;
                }
                indexMap[origIndex] = i;
            }
        });

        angular.forEach(self.config.columnDefs, function(colDef, i) {
                // Get the ngColumn that matches the current column from columnDefs
            var ngColumn = $scope.columns[indexMap[i]];

            colDef.index = i;

            var isPercent = false, t;
            //if width is not defined, set it to a single star
            if ($utils.isNullOrUndefined(colDef.width)) {
                colDef.width = "*";
            } else { // get column width
                isPercent = isNaN(colDef.width) ? $utils.endsWith(colDef.width, "%") : false;
                t = isPercent ? colDef.width : parseInt(colDef.width, 10);
            }

             // check if it is a number
            if (isNaN(t) && !$scope.hasUserChangedGridColumnWidths) {
                t = colDef.width;
                // figure out if the width is defined or if we need to calculate it
                if (t === 'auto') { // set it for now until we have data and subscribe when it changes so we can set the width.
                    ngColumn.width = ngColumn.minWidth;
                    totalWidth += ngColumn.width;
                    var temp = ngColumn;

                    $scope.$on("ngGridEventData", function () {
                        self.resizeOnData(temp);
                    });
                    return;
                } else if (t.indexOf("*") !== -1) { //  we need to save it until the end to do the calulations on the remaining width.
                    if (ngColumn.visible !== false) {
                        asteriskNum += t.length;
                    }
                    asterisksArray.push(colDef);
                    return;
                } else if (isPercent) { // If the width is a percentage, save it until the very last.
                    percentArray.push(colDef);
                    return;
                } else { // we can't parse the width so lets throw an error.
                    throw "unable to parse column width, use percentage (\"10%\",\"20%\", etc...) or \"*\" to use remaining width of grid";
                }
            } else if (ngColumn.visible !== false) {
                totalWidth += ngColumn.width = parseInt(ngColumn.width, 10);
            }
        });
        
        // Now we check if we saved any percentage columns for calculating last
        if (percentArray.length > 0) {
            //If they specificy for maintain column ratios to be false in grid config, then it will remain false. If not specifiied or true, will be true.
            self.config.maintainColumnRatios = self.config.maintainColumnRatios !== false; 
            // If any columns with % widths have been hidden, then let other % based columns use their width
            var percentWidth = 0; // The total % value for all columns setting their width using % (will e.g. be 40 for 2 columns with 20% each)
            var hiddenPercent = 0; // The total % value for all columns setting their width using %, but which have been hidden
            angular.forEach(percentArray, function(colDef) {
                // Get the ngColumn that matches the current column from columnDefs
                var ngColumn = $scope.columns[indexMap[colDef.index]];
                var t = colDef.width;
                var percent = parseInt(t.slice(0, -1), 10) / 100;
                percentWidth += percent;

                if (!ngColumn.visible) {
                    hiddenPercent += percent;
                }
            });
            var percentWidthUsed = percentWidth - hiddenPercent;

            // do the math
            angular.forEach(percentArray, function(colDef) {
                // Get the ngColumn that matches the current column from columnDefs
                var ngColumn = $scope.columns[indexMap[colDef.index]];
                
                // Calc the % relative to the amount of % reserved for the visible columns (that use % based widths)
                var t = colDef.width;
                var percent = parseInt(t.slice(0, -1), 10) / 100;
                if (hiddenPercent > 0) {
                    percent = percent / percentWidthUsed;
                }
                else {
                    percent = percent / percentWidth;
                }

                var pixelsForPercentBasedWidth = self.rootDim.outerWidth * percentWidth;
                ngColumn.width = Math.floor(pixelsForPercentBasedWidth * percent);
                totalWidth += ngColumn.width;
            });
        }

        // check if we saved any asterisk columns for calculating later
        if (asterisksArray.length > 0) {
            //If they specificy for maintain column ratios to be false in grid config, then it will remain false. If not specifiied or true, will be true.
            self.config.maintainColumnRatios = self.config.maintainColumnRatios !== false; 
            // get the remaining width
            var remainingWidth = self.rootDim.outerWidth - totalWidth;
            // are we overflowing vertically?
            if (self.maxCanvasHt > $scope.viewportDimHeight()) {
                //compensate for scrollbar
                remainingWidth -= domUtilityService.ScrollW;
            }
            // calculate the weight of each asterisk rounded down
            var asteriskVal = Math.floor(remainingWidth / asteriskNum);

            // set the width of each column based on the number of stars
            angular.forEach(asterisksArray, function(colDef, i) {
                // Get the ngColumn that matches the current column from columnDefs
                var ngColumn = $scope.columns[indexMap[colDef.index]];                
                ngColumn.width = asteriskVal * colDef.width.length;
                if (ngColumn.visible !== false) {
                    totalWidth += ngColumn.width;
                }

                var isLast = (i === (asterisksArray.length - 1));
                //if last asterisk and doesn't fill width of grid, add the difference
                if(isLast && totalWidth < self.rootDim.outerWidth){
                    var gridWidthDifference = self.rootDim.outerWidth - totalWidth;
                    if(self.maxCanvasHt > $scope.viewportDimHeight()){
                        gridWidthDifference -= domUtilityService.ScrollW;
                    }
                    ngColumn.width += gridWidthDifference;
                }
            });
        }
    };
    self.init = function() {
        return self.initTemplates().then(function(){
            //factories and services
            $scope.selectionProvider = new ngSelectionProvider(self, $scope, $parse);
            $scope.domAccessProvider = new ngDomAccessProvider(self);
            self.rowFactory = new ngRowFactory(self, $scope, domUtilityService, $templateCache, $utils);
            self.searchProvider = new ngSearchProvider($scope, self, $filter);
            self.styleProvider = new ngStyleProvider($scope, self);
            $scope.$watch('configGroups', function(a) {
              var tempArr = [];
              angular.forEach(a, function(item) {
                tempArr.push(item.field || item);
              });
              self.config.groups = tempArr;
              self.rowFactory.filteredRowsChanged();
              $scope.$emit('ngGridEventGroups', a);
            }, true);
            $scope.$watch('columns', function (a) {
                if(!$scope.isColumnResizing){
                    domUtilityService.RebuildGrid($scope, self);
                }
                $scope.$emit('ngGridEventColumns', a);
            }, true);
            $scope.$watch(function() {
                return options.i18n;
            }, function(newLang) {
                $utils.seti18n($scope, newLang);
            });
            self.maxCanvasHt = self.calcMaxCanvasHeight();

            if (self.config.sortInfo.fields && self.config.sortInfo.fields.length > 0) {
                $scope.$watch(function() {
                    return self.config.sortInfo;
                }, function(sortInfo){
                    if (!sortService.isSorting) {
                        self.sortColumnsInit();
                        $scope.$emit('ngGridEventSorted', self.config.sortInfo);
                    }
                },true);
            }
        });

        // var p = $q.defer();
        // p.resolve();
        // return p.promise;
    };
   
    self.resizeOnData = function(col) {
        // we calculate the longest data.
        var longest = col.minWidth;
        var arr = $utils.getElementsByClassName('col' + col.index);
        angular.forEach(arr, function(elem, index) {
            var i;
            if (index === 0) {
                var kgHeaderText = $(elem).find('.ngHeaderText');
                i = $utils.visualLength(kgHeaderText) + 10; // +10 some margin
            } else {
                var ngCellText = $(elem).find('.ngCellText');
                i = $utils.visualLength(ngCellText) + 10; // +10 some margin
            }
            if (i > longest) {
                longest = i;
            }
        });
        col.width = col.longest = Math.min(col.maxWidth, longest + 7); // + 7 px to make it look decent.
        domUtilityService.BuildStyles($scope, self, true);
    };
    self.lastSortedColumns = [];
    self.sortData = function(col, evt) {
        if (evt && evt.shiftKey && self.config.sortInfo) {
            var indx = self.config.sortInfo.columns.indexOf(col);
            if (indx === -1) {
                if (self.config.sortInfo.columns.length === 1) {
                    self.config.sortInfo.columns[0].sortPriority = 1;
                }
                self.config.sortInfo.columns.push(col);
                col.sortPriority = self.config.sortInfo.columns.length;
                self.config.sortInfo.fields.push(col.field);
                self.config.sortInfo.directions.push(col.sortDirection);
                self.lastSortedColumns.push(col);
            } else {
                self.config.sortInfo.directions[indx] = col.sortDirection;
            }
        } else {
            var isArr = $.isArray(col);
            self.config.sortInfo.columns.length = 0;
            self.config.sortInfo.fields.length = 0;
            self.config.sortInfo.directions.length = 0;
            var push = function (c) {
                self.config.sortInfo.columns.push(c);
                self.config.sortInfo.fields.push(c.field);
                self.config.sortInfo.directions.push(c.sortDirection);
                self.lastSortedColumns.push(c);
            };
            if (isArr) {
                self.clearSortingData();
                angular.forEach(col, function (c, i) {
                    c.sortPriority = i + 1;
                    push(c);
                });
            } else {
                self.clearSortingData(col);
                col.sortPriority = undefined;
                push(col);
            }
        }
        self.sortActual();
        self.searchProvider.evalFilter();
        $scope.$emit('ngGridEventSorted', self.config.sortInfo);
    };
    self.sortColumnsInit = function() {
        if (self.config.sortInfo.columns) {
            self.config.sortInfo.columns.length = 0;
        } else {
            self.config.sortInfo.columns = [];
        }
        angular.forEach($scope.columns, function(c) {
            var i = self.config.sortInfo.fields.indexOf(c.field);
            if (i !== -1) {
                c.sortDirection = self.config.sortInfo.directions[i] || 'asc';
                self.config.sortInfo.columns[i] = c;
            }
        });
        angular.forEach(self.config.sortInfo.columns, function(c){
            self.sortData(c);
        });
    };
    self.sortActual = function() {
        if (!self.config.useExternalSorting) {
            var tempData = self.data.slice(0);
            angular.forEach(tempData, function(item, i) {
                var e = self.rowMap[i];
                if (e !== undefined) {
                    var v = self.rowCache[e];
                    if (v !== undefined) {
                        item.preSortSelected = v.selected;
                        item.preSortIndex = i;
                    }
                }
            });
            sortService.Sort(self.config.sortInfo, tempData);
            angular.forEach(tempData, function(item, i) {
                self.rowCache[i].entity = item;
                self.rowCache[i].selected = item.preSortSelected;
                self.rowMap[item.preSortIndex] = i;
                delete item.preSortSelected;
                delete item.preSortIndex;
            });
        }
    };

    self.clearSortingData = function (col) {
        if (!col) {
            angular.forEach(self.lastSortedColumns, function (c) {
                c.sortDirection = "";
                c.sortPriority = null;
            });
            self.lastSortedColumns = [];
        } else {
            angular.forEach(self.lastSortedColumns, function (c) {
                if (col.index !== c.index) {
                    c.sortDirection = "";
                    c.sortPriority = null;
                }
            });
            self.lastSortedColumns[0] = col;
            self.lastSortedColumns.length = 1;
        }
    };
    self.fixColumnIndexes = function() {
        //fix column indexes
        for (var i = 0; i < $scope.columns.length; i++) {
            $scope.columns[i].index = i;
        }
    };
    self.fixGroupIndexes = function() {
        angular.forEach($scope.configGroups, function(item, i) {
            item.groupIndex = i + 1;
        });
    };
    //$scope vars
    $scope.elementsNeedMeasuring = true;
    $scope.columns = [];
    $scope.renderedRows = [];
    $scope.renderedColumns = [];
    $scope.headerRow = null;
    $scope.rowHeight = self.config.rowHeight;
    $scope.jqueryUITheme = self.config.jqueryUITheme;
    $scope.showSelectionCheckbox = self.config.showSelectionCheckbox;
    $scope.enableCellSelection = self.config.enableCellSelection;
    $scope.enableCellEditOnFocus = self.config.enableCellEditOnFocus;
    $scope.footer = null;
    $scope.selectedItems = self.config.selectedItems;
    $scope.multiSelect = self.config.multiSelect;
    $scope.showFooter = self.config.showFooter;
    $scope.footerRowHeight = $scope.showFooter ? self.config.footerRowHeight : 0;
    $scope.showColumnMenu = self.config.showColumnMenu;
    $scope.showMenu = false;
    $scope.configGroups = [];
    $scope.gridId = self.gridId;
    //Paging
    $scope.enablePaging = self.config.enablePaging;
    $scope.pagingOptions = self.config.pagingOptions;

    //i18n support
    $scope.i18n = {};
    $utils.seti18n($scope, self.config.i18n);
    $scope.adjustScrollLeft = function (scrollLeft) {
        var colwidths = 0,
            totalLeft = 0,
            x = $scope.columns.length,
            newCols = [],
            dcv = !self.config.enableColumnHeavyVirt;
        var r = 0;
        var addCol = function (c) {
            if (dcv) {
                newCols.push(c);
            } else {
                if (!$scope.renderedColumns[r]) {
                    $scope.renderedColumns[r] = c.copy();
                } else {
                    $scope.renderedColumns[r].setVars(c);
                }
            }
            r++;
        };
        for (var i = 0; i < x; i++) {
            var col = $scope.columns[i];
            if (col.visible !== false) {
                var w = col.width + colwidths;
                if (col.pinned) {
                    addCol(col);
                    var newLeft = i > 0 ? (scrollLeft + totalLeft) : scrollLeft;
                    domUtilityService.setColLeft(col, newLeft, self);
                    totalLeft += col.width;
                } else {
                    if (w >= scrollLeft) {
                        if (colwidths <= scrollLeft + self.rootDim.outerWidth) {
                            addCol(col);
                        }
                    }
                }
                colwidths += col.width;
            }
        }
        if (dcv) {
            $scope.renderedColumns = newCols;
        }
    };
    self.prevScrollTop = 0;
    self.prevScrollIndex = 0;
    $scope.adjustScrollTop = function(scrollTop, force) {
        if (self.prevScrollTop === scrollTop && !force) {
            return;
        }
        if (scrollTop > 0 && self.$viewport[0].scrollHeight - scrollTop <= self.$viewport.outerHeight()) {
            $scope.$emit('ngGridEventScroll');
        }
        var rowIndex = Math.floor(scrollTop / self.config.rowHeight);
        var newRange;
        if (self.filteredRows.length > self.config.virtualizationThreshold) {
            // Have we hit the threshold going down?
            if (self.prevScrollTop < scrollTop && rowIndex < self.prevScrollIndex + SCROLL_THRESHOLD) {
                return;
            }
            //Have we hit the threshold going up?
            if (self.prevScrollTop > scrollTop && rowIndex > self.prevScrollIndex - SCROLL_THRESHOLD) {
                return;
            }
            newRange = new ngRange(Math.max(0, rowIndex - EXCESS_ROWS), rowIndex + self.minRowsToRender() + EXCESS_ROWS);
        } else {
            var maxLen = $scope.configGroups.length > 0 ? self.rowFactory.parsedData.length : self.data.length;
            newRange = new ngRange(0, Math.max(maxLen, self.minRowsToRender() + EXCESS_ROWS));
        }
        self.prevScrollTop = scrollTop;
        self.rowFactory.UpdateViewableRange(newRange);
        self.prevScrollIndex = rowIndex;
    };

    //scope funcs
    $scope.toggleShowMenu = function() {
        $scope.showMenu = !$scope.showMenu;
    };
    $scope.toggleSelectAll = function(state, selectOnlyVisible) {
        $scope.selectionProvider.toggleSelectAll(state, false, selectOnlyVisible);
    };
    $scope.totalFilteredItemsLength = function() {
        return self.filteredRows.length;
    };
    $scope.showGroupPanel = function() {
        return self.config.showGroupPanel;
    };
    $scope.topPanelHeight = function() {
        return self.config.showGroupPanel === true ? self.config.headerRowHeight + 32 : self.config.headerRowHeight;
    };

    $scope.viewportDimHeight = function() {
        return Math.max(0, self.rootDim.outerHeight - $scope.topPanelHeight() - $scope.footerRowHeight - 2);
    };
    $scope.groupBy = function (col) {
        if (self.data.length < 1 || !col.groupable  || !col.field) {
            return;
        }
        //first sort the column
        if (!col.sortDirection) {
            col.sort({ shiftKey: $scope.configGroups.length > 0 ? true : false });
        }

        var indx = $scope.configGroups.indexOf(col);
        if (indx === -1) {
            col.isGroupedBy = true;
            $scope.configGroups.push(col);
            col.groupIndex = $scope.configGroups.length;
        } else {
            $scope.removeGroup(indx);
        }
        self.$viewport.scrollTop(0);
        domUtilityService.digest($scope);
    };
    $scope.removeGroup = function(index) {
        var col = $scope.columns.filter(function(item) {
            return item.groupIndex === (index + 1);
        })[0];
        col.isGroupedBy = false;
        col.groupIndex = 0;
        if ($scope.columns[index].isAggCol) {
            $scope.columns.splice(index, 1);
            $scope.configGroups.splice(index, 1);
            self.fixGroupIndexes();
        }
        if ($scope.configGroups.length === 0) {
            self.fixColumnIndexes();
            domUtilityService.digest($scope);
        }
        $scope.adjustScrollLeft(0);
    };
    $scope.togglePin = function (col) {
        var indexFrom = col.index;
        var indexTo = 0;
        for (var i = 0; i < $scope.columns.length; i++) {
            if (!$scope.columns[i].pinned) {
                break;
            }
            indexTo++;
        }
        if (col.pinned) {
            indexTo = Math.max(col.originalIndex, indexTo - 1);
        }
        col.pinned = !col.pinned;
        // Splice the columns
        $scope.columns.splice(indexFrom, 1);
        $scope.columns.splice(indexTo, 0, col);
        self.fixColumnIndexes();
        // Finally, rebuild the CSS styles.
        domUtilityService.BuildStyles($scope, self, true);
        self.$viewport.scrollLeft(self.$viewport.scrollLeft() - col.width);
    };
    $scope.totalRowWidth = function() {
        var totalWidth = 0,
            cols = $scope.columns;
        for (var i = 0; i < cols.length; i++) {
            if (cols[i].visible !== false) {
                totalWidth += cols[i].width;
            }
        }
        return totalWidth;
    };
    $scope.headerScrollerDim = function() {
        var viewportH = $scope.viewportDimHeight(),
            maxHeight = self.maxCanvasHt,
            vScrollBarIsOpen = (maxHeight > viewportH),
            newDim = new ngDimension();

        newDim.autoFitHeight = true;
        newDim.outerWidth = $scope.totalRowWidth();
        if (vScrollBarIsOpen) {
            newDim.outerWidth += self.elementDims.scrollW;
        } else if ((maxHeight - viewportH) <= self.elementDims.scrollH) { //if the horizontal scroll is open it forces the viewport to be smaller
            newDim.outerWidth += self.elementDims.scrollW;
        }
        return newDim;
    };
};

var ngRange = function (top, bottom) {
    this.topRow = top;
    this.bottomRow = bottom;
};
var ngRow = function (entity, config, selectionProvider, rowIndex, $utils) {
	this.entity = entity;
	this.config = config;
	this.selectionProvider = selectionProvider;
	this.rowIndex = rowIndex;
	this.utils = $utils;
	this.selected = selectionProvider.getSelection(entity);
	this.cursor = this.config.enableRowSelection ? 'pointer' : 'default';
	this.beforeSelectionChange = config.beforeSelectionChangeCallback;
	this.afterSelectionChange = config.afterSelectionChangeCallback;
	this.offsetTop = this.rowIndex * config.rowHeight;
	this.rowDisplayIndex = 0;
};

ngRow.prototype.setSelection = function (isSelected) {
	this.selectionProvider.setSelection(this, isSelected);
	this.selectionProvider.lastClickedRow = this;
};
ngRow.prototype.continueSelection = function (event) {
	this.selectionProvider.ChangeSelection(this, event);
};
ngRow.prototype.ensureEntity = function (expected) {
	if (this.entity !== expected) {
		// Update the entity and determine our selected property
		this.entity = expected;
		this.selected = this.selectionProvider.getSelection(this.entity);
	}
};
ngRow.prototype.toggleSelected = function (event) {
	if (!this.config.enableRowSelection && !this.config.enableCellSelection) {
		return true;
	}
	var element = event.target || event;
	//check and make sure its not the bubbling up of our checked 'click' event 
	if (element.type === "checkbox" && element.parentElement.className !== "ngSelectionCell ng-scope") {
		return true;
	}
	if (this.config.selectWithCheckboxOnly && element.type !== "checkbox") {
		this.selectionProvider.lastClickedRow = this;
		return true;
	} 
	if (this.beforeSelectionChange(this, event)) {
		this.continueSelection(event);
	}
	return false;
};
ngRow.prototype.alternatingRowClass = function () {
	var isEven = (this.rowIndex % 2) === 0;
	var classes = {
		'ngRow' : true,
		'selected': this.selected,
		'even': isEven,
		'odd': !isEven,
		'ui-state-default': this.config.jqueryUITheme && isEven,
		'ui-state-active': this.config.jqueryUITheme && !isEven
	};
	return classes;
};
ngRow.prototype.getProperty = function (path) {
	return this.utils.evalProperty(this.entity, path);
};
ngRow.prototype.copy = function () {
	this.clone = new ngRow(this.entity, this.config, this.selectionProvider, this.rowIndex, this.utils);
	this.clone.isClone = true;
	this.clone.elm = this.elm;
	this.clone.orig = this;
	return this.clone;
};
ngRow.prototype.setVars = function (fromRow) {
	fromRow.clone = this;
	this.entity = fromRow.entity;
	this.selected = fromRow.selected;
    this.orig = fromRow;
};
var ngRowFactory = function (grid, $scope, domUtilityService, $templateCache, $utils) {
    var self = this;
    // we cache rows when they are built, and then blow the cache away when sorting
    self.aggCache = {};
    self.parentCache = []; // Used for grouping and is cleared each time groups are calulated.
    self.dataChanged = true;
    self.parsedData = [];
    self.rowConfig = {};
    self.selectionProvider = $scope.selectionProvider;
    self.rowHeight = 30;
    self.numberOfAggregates = 0;
    self.groupedData = undefined;
    self.rowHeight = grid.config.rowHeight;
    self.rowConfig = {
        enableRowSelection: grid.config.enableRowSelection,
        rowClasses: grid.config.rowClasses,
        selectedItems: $scope.selectedItems,
        selectWithCheckboxOnly: grid.config.selectWithCheckboxOnly,
        beforeSelectionChangeCallback: grid.config.beforeSelectionChange,
        afterSelectionChangeCallback: grid.config.afterSelectionChange,
        jqueryUITheme: grid.config.jqueryUITheme,
        enableCellSelection: grid.config.enableCellSelection,
        rowHeight: grid.config.rowHeight
    };

    self.renderedRange = new ngRange(0, grid.minRowsToRender() + EXCESS_ROWS);

    // @entity - the data item
    // @rowIndex - the index of the row
    self.buildEntityRow = function(entity, rowIndex) {
        // build the row
        return new ngRow(entity, self.rowConfig, self.selectionProvider, rowIndex, $utils);
    };

    self.buildAggregateRow = function(aggEntity, rowIndex) {
        var agg = self.aggCache[aggEntity.aggIndex]; // first check to see if we've already built it 
        if (!agg) {
            // build the row
            agg = new ngAggregate(aggEntity, self, self.rowConfig.rowHeight, grid.config.groupsCollapsedByDefault);
            self.aggCache[aggEntity.aggIndex] = agg;
        }
        agg.rowIndex = rowIndex;
        agg.offsetTop = rowIndex * self.rowConfig.rowHeight;
        return agg;
    };
    self.UpdateViewableRange = function(newRange) {
        self.renderedRange = newRange;
        self.renderedChange();
    };
    self.filteredRowsChanged = function() {
        // check for latebound autogenerated columns
        if (grid.lateBoundColumns && grid.filteredRows.length > 0) {
            grid.config.columnDefs = undefined;
            grid.buildColumns();
            grid.lateBoundColumns = false;
            $scope.$evalAsync(function() {
                $scope.adjustScrollLeft(0);
            });
        }
        self.dataChanged = true;
        if (grid.config.groups.length > 0) {
            self.getGrouping(grid.config.groups);
        }
        self.UpdateViewableRange(self.renderedRange);
    };

    self.renderedChange = function() {
        if (!self.groupedData || grid.config.groups.length < 1) {
            self.renderedChangeNoGroups();
            grid.refreshDomSizes();
            return;
        }
        self.wasGrouped = true;
        self.parentCache = [];
        var x = 0;
        var temp = self.parsedData.filter(function (e) {
            if (e.isAggRow) {
                if (e.parent && e.parent.collapsed) {
                    return false;
                }
                return true;
            }
            if (!e[NG_HIDDEN]) {
                e.rowIndex = x++;
            }
            return !e[NG_HIDDEN];
        });
        self.totalRows = temp.length;
        var rowArr = [];
        for (var i = self.renderedRange.topRow; i < self.renderedRange.bottomRow; i++) {
            if (temp[i]) {
                temp[i].offsetTop = i * grid.config.rowHeight;
                rowArr.push(temp[i]);
            }
        }
        grid.setRenderedRows(rowArr);
    };

    self.renderedChangeNoGroups = function () {
        var rowArr = [];
        for (var i = self.renderedRange.topRow; i < self.renderedRange.bottomRow; i++) {
            if (grid.filteredRows[i]) {
                grid.filteredRows[i].rowIndex = i;
                grid.filteredRows[i].offsetTop = i * grid.config.rowHeight;
                rowArr.push(grid.filteredRows[i]);
            }
        }
        grid.setRenderedRows(rowArr);
    };

    self.fixRowCache = function () {
        var newLen = grid.data.length;
        var diff = newLen - grid.rowCache.length;
        if (diff < 0) {
            grid.rowCache.length = grid.rowMap.length = newLen;
        } else {
            for (var i = grid.rowCache.length; i < newLen; i++) {
                grid.rowCache[i] = grid.rowFactory.buildEntityRow(grid.data[i], i);
            }
        }
    };

    //magical recursion. it works. I swear it. I figured it out in the shower one day.
    self.parseGroupData = function(g) {
        if (g.values) {
            for (var x = 0; x < g.values.length; x++){
                // get the last parent in the array because that's where our children want to be
                self.parentCache[self.parentCache.length - 1].children.push(g.values[x]);
                //add the row to our return array
                self.parsedData.push(g.values[x]);
            }
        } else {
            for (var prop in g) {
                // exclude the meta properties.
                if (prop === NG_FIELD || prop === NG_DEPTH || prop === NG_COLUMN) {
                    continue;
                } else if (g.hasOwnProperty(prop)) {
                    //build the aggregate row
                    var agg = self.buildAggregateRow({
                        gField: g[NG_FIELD],
                        gLabel: prop,
                        gDepth: g[NG_DEPTH],
                        isAggRow: true,
                        '_ng_hidden_': false,
                        children: [],
                        aggChildren: [],
                        aggIndex: self.numberOfAggregates,
                        aggLabelFilter: g[NG_COLUMN].aggLabelFilter
                    }, 0);
                    self.numberOfAggregates++;
                    //set the aggregate parent to the parent in the array that is one less deep.
                    agg.parent = self.parentCache[agg.depth - 1];
                    // if we have a parent, set the parent to not be collapsed and append the current agg to its children
                    if (agg.parent) {
                        agg.parent.collapsed = false;
                        agg.parent.aggChildren.push(agg);
                    }
                    // add the aggregate row to the parsed data.
                    self.parsedData.push(agg);
                    // the current aggregate now the parent of the current depth
                    self.parentCache[agg.depth] = agg;
                    // dig deeper for more aggregates or children.
                    self.parseGroupData(g[prop]);
                }
            }
        }
    };
    //Shuffle the data into their respective groupings.
    self.getGrouping = function(groups) {
        self.aggCache = [];
        self.numberOfAggregates = 0;
        self.groupedData = {};
        // Here we set the onmousedown event handler to the header container.
        var rows = grid.filteredRows,
            maxDepth = groups.length,
            cols = $scope.columns;

        function filterCols(cols, group) {
            return cols.filter(function(c) {
                return c.field === group;
            });
        }

        for (var x = 0; x < rows.length; x++) {
            var model = rows[x].entity;
            if (!model) {
                return;
            }
            rows[x][NG_HIDDEN] = grid.config.groupsCollapsedByDefault;
            var ptr = self.groupedData;

            for (var y = 0; y < groups.length; y++) {
                var group = groups[y];

                var col = filterCols(cols, group)[0];

                var val = $utils.evalProperty(model, group);
                val = val ? val.toString() : 'null';
                if (!ptr[val]) {
                    ptr[val] = {};
                }
                if (!ptr[NG_FIELD]) {
                    ptr[NG_FIELD] = group;
                }
                if (!ptr[NG_DEPTH]) {
                    ptr[NG_DEPTH] = y;
                }
                if (!ptr[NG_COLUMN]) {
                    ptr[NG_COLUMN] = col;
                }
                ptr = ptr[val];
            }
            if (!ptr.values) {
                ptr.values = [];
            }
            ptr.values.push(rows[x]);
        }

        //moved out of above loops due to if no data initially, but has initial grouping, columns won't be added
        if(cols.length > 0) {
            for (var z = 0; z < groups.length; z++) {
                if (!cols[z].isAggCol && z <= maxDepth) {
                    cols.splice(0, 0, new ngColumn({
                        colDef: {
                            field: '',
                            width: 25,
                            sortable: false,
                            resizable: false,
                            headerCellTemplate: '<div class="ngAggHeader"></div>',
                            pinned: grid.config.pinSelectionCheckbox
                            
                        },
                        enablePinning: grid.config.enablePinning,
                        isAggCol: true,
                        headerRowHeight: grid.config.headerRowHeight
                        
                    }, $scope, grid, domUtilityService, $templateCache, $utils));
                }
            }
        }

        grid.fixColumnIndexes();
        $scope.adjustScrollLeft(0);
        self.parsedData.length = 0;
        self.parseGroupData(self.groupedData);
        self.fixRowCache();
    };

    if (grid.config.groups.length > 0 && grid.filteredRows.length > 0) {
        self.getGrouping(grid.config.groups);
    }
};
var ngSearchProvider = function ($scope, grid, $filter) {
    var self = this,
        searchConditions = [];

    self.extFilter = grid.config.filterOptions.useExternalFilter;
    $scope.showFilter = grid.config.showFilter;
    $scope.filterText = '';

    self.fieldMap = {};

    var searchEntireRow = function(condition, item, fieldMap){
        var result;
        for (var prop in item) {
            if (item.hasOwnProperty(prop)) {
                var c = fieldMap[prop.toLowerCase()];
                if (!c) {
                    continue;
                }
                var pVal = item[prop];
                if(typeof pVal === 'object'){
	                  if (searchEntireRow(condition, pVal, c)) {
		                  return true;
	                  }
                } else {
                    var f = null,
                        s = null;
                    if (c && c.cellFilter) {
                        s = c.cellFilter.split(':');
                        f = $filter(s[0]);
                    }
                    if (pVal !== null && pVal !== undefined) {
                        if (typeof f === "function") {
                            var filterRes = f(pVal, s[1]).toString();
                            result = condition.regex.test(filterRes);
                        } else {
                            result = condition.regex.test(pVal.toString());
                        }
                        if (result) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    };

    var searchColumn = function(condition, item){
        var result;
        var col = self.fieldMap[condition.columnDisplay];
        if (!col) {
            return false;
        }
        var sp = col.cellFilter.split(':');
        var filter = col.cellFilter ? $filter(sp[0]) : null;
        var value = item[condition.column] || item[col.field.split('.')[0]];
        if (value === null || value === undefined) {
            return false;
        }
        if (typeof filter === "function") {
            var filterResults = filter(typeof value === "object" ? evalObject(value, col.field) : value, sp[1]).toString();
            result = condition.regex.test(filterResults);
        }
        else {
            result = condition.regex.test(typeof value === "object" ? evalObject(value, col.field).toString() : value.toString());
        }
        if (result) {
            return true;
        }
        return false;
    };

    var filterFunc = function(item) {
        for (var x = 0, len = searchConditions.length; x < len; x++) {
            var condition = searchConditions[x];
            var result;
            if (!condition.column) {
                result = searchEntireRow(condition, item, self.fieldMap);
            } else {
                result = searchColumn(condition, item);
            }     
            if(!result) {
                return false;
            }      
        }
        return true;
    };

    self.evalFilter = function () {
        if (searchConditions.length === 0) {
            grid.filteredRows = grid.rowCache;
        } else {
            grid.filteredRows = grid.rowCache.filter(function(row) {
                return filterFunc(row.entity);
            });
        }
        for (var i = 0; i < grid.filteredRows.length; i++)
        {
            grid.filteredRows[i].rowIndex = i;
            
        }
        grid.rowFactory.filteredRowsChanged();
    };

    //Traversing through the object to find the value that we want. If fail, then return the original object.
    var evalObject = function (obj, columnName) {
        if (typeof obj !== "object" || typeof columnName !== "string") {
            return obj;
        }
        var args = columnName.split('.');
        var cObj = obj;
        if (args.length > 1) {
            for (var i = 1, len = args.length; i < len; i++) {
                cObj = cObj[args[i]];
                if (!cObj) {
                    return obj;
                }
            }
            return cObj;
        }
        return obj;
    };
    var getRegExp = function (str, modifiers) {
        try {
            return new RegExp(str, modifiers);
        } catch (err) {
            //Escape all RegExp metacharacters.
            return new RegExp(str.replace(/(\^|\$|\(|\)|<|>|\[|\]|\{|\}|\\|\||\.|\*|\+|\?)/g, '\\$1'));
        }
    };
    var buildSearchConditions = function (a) {
        //reset.
        searchConditions = [];
        var qStr;
        if (!(qStr = $.trim(a))) {
            return;
        }
        var columnFilters = qStr.split(";");
        for (var i = 0; i < columnFilters.length; i++) {
            var args = columnFilters[i].split(':');
            if (args.length > 1) {
                var columnName = $.trim(args[0]);
                var columnValue = $.trim(args[1]);
                if (columnName && columnValue) {
                    searchConditions.push({
                        column: columnName,
                        columnDisplay: columnName.replace(/\s+/g, '').toLowerCase(),
                        regex: getRegExp(columnValue, 'i')
                    });
                }
            } else {
                var val = $.trim(args[0]);
                if (val) {
                    searchConditions.push({
                        column: '',
                        regex: getRegExp(val, 'i')
                    });
                }
            }
        }
    };

    if (!self.extFilter) {
        $scope.$watch('columns', function (cs) {
            for (var i = 0; i < cs.length; i++) {
                var col = cs[i];
                if (col.field) {
                    if(col.field.match(/\./g)){
                        var properties = col.field.split('.');
                        var currentProperty = self.fieldMap;
                        for(var j = 0; j < properties.length - 1; j++) {
                            currentProperty[ properties[j] ] =  currentProperty[ properties[j] ] || {};
                            currentProperty = currentProperty[properties[j]];
                        }
                        currentProperty[ properties[properties.length - 1] ] = col;
                    } else {
                        self.fieldMap[col.field.toLowerCase()] = col;
                    }
                }
                if (col.displayName) {
                    self.fieldMap[col.displayName.toLowerCase().replace(/\s+/g, '')] = col;
                }
            }
        });
    }

    $scope.$watch(
        function () {
            return grid.config.filterOptions.filterText;
        },
        function (a) {
            $scope.filterText = a;
        }
    );

    $scope.$watch('filterText', function(a){
        if (!self.extFilter) {
            $scope.$emit('ngGridEventFilter', a);
            buildSearchConditions(a);
            self.evalFilter();
        }
    });
};
var ngSelectionProvider = function (grid, $scope, $parse) {
    var self = this;
    self.multi = grid.config.multiSelect;
    self.selectedItems = grid.config.selectedItems;
    self.selectedIndex = grid.config.selectedIndex;
    self.lastClickedRow = undefined;
    self.ignoreSelectedItemChanges = false; // flag to prevent circular event loops keeping single-select var in sync
    self.pKeyParser = $parse(grid.config.primaryKey);

    // function to manage the selection action of a data item (entity)
    self.ChangeSelection = function (rowItem, evt) {
        // ctrl-click + shift-click multi-selections
        // up/down key navigation in multi-selections
        var charCode = evt.which || evt.keyCode;
        var isUpDownKeyPress = (charCode === 40 || charCode === 38);

        if (evt && evt.shiftKey && !evt.keyCode && self.multi && grid.config.enableRowSelection) {
            if (self.lastClickedRow) {
                var rowsArr;
                if ($scope.configGroups.length > 0) {
                    rowsArr = grid.rowFactory.parsedData.filter(function(row) {
                        return !row.isAggRow;
                    });
                }
                else {
                    rowsArr = grid.filteredRows;
                }

                var thisIndx = rowItem.rowIndex;
                var prevIndx = self.lastClickedRowIndex;
                
                if (thisIndx === prevIndx) {
                    return false;
                }

                if (thisIndx < prevIndx) {
                    thisIndx = thisIndx ^ prevIndx;
                    prevIndx = thisIndx ^ prevIndx;
                    thisIndx = thisIndx ^ prevIndx;
                    thisIndx--;
                }
                else {
                    prevIndx++;
                }

                var rows = [];
                for (; prevIndx <= thisIndx; prevIndx++) {
                    rows.push(rowsArr[prevIndx]);
                }

                if (rows[rows.length - 1].beforeSelectionChange(rows, evt)) {
                    for (var i = 0; i < rows.length; i++) {
                        var ri = rows[i];
                        var selectionState = ri.selected;
                        ri.selected = !selectionState;
                        if (ri.clone) {
                            ri.clone.selected = ri.selected;
                        }
                        var index = self.selectedItems.indexOf(ri.entity);
                        if (index === -1) {
                            self.selectedItems.push(ri.entity);
                        }
                        else {
                            self.selectedItems.splice(index, 1);
                        }
                    }
                    rows[rows.length - 1].afterSelectionChange(rows, evt);
                }
                self.lastClickedRow = rowItem;
                self.lastClickedRowIndex = rowItem.rowIndex;

                return true;
            }
        }
        else if (!self.multi) {
            if (self.lastClickedRow === rowItem) {
                self.setSelection(self.lastClickedRow, grid.config.keepLastSelected ? true : !rowItem.selected);
            } else {
                if (self.lastClickedRow) {
                    self.setSelection(self.lastClickedRow, false);
                }
                self.setSelection(rowItem, !rowItem.selected);
            }
        }
        else if (!evt.keyCode || isUpDownKeyPress && !grid.config.selectWithCheckboxOnly) {
            self.setSelection(rowItem, !rowItem.selected);
        }
        self.lastClickedRow = rowItem;
        self.lastClickedRowIndex = rowItem.rowIndex;
        return true;
    };

    self.getSelection = function (entity) {
        var isSelected = false;
        if (grid.config.primaryKey) {
            var val = self.pKeyParser(entity);
            angular.forEach(self.selectedItems, function (c) {
                if (val === self.pKeyParser(c)) {
                    isSelected = true;
                }
            });
        }
        else {
            isSelected = self.selectedItems.indexOf(entity) !== -1;
        }
        return isSelected;
    };

    // just call this func and hand it the rowItem you want to select (or de-select)    
    self.setSelection = function (rowItem, isSelected) {
        if(grid.config.enableRowSelection){
            if (!isSelected) {
                var indx = self.selectedItems.indexOf(rowItem.entity);
                if (indx !== -1) {
                    self.selectedItems.splice(indx, 1);
                }
            }
            else {
                if (self.selectedItems.indexOf(rowItem.entity) === -1) {
                    if (!self.multi && self.selectedItems.length > 0) {
                        self.toggleSelectAll(false, true);
                    }
                    self.selectedItems.push(rowItem.entity);
                }
            }
            rowItem.selected = isSelected;
            if (rowItem.orig) {
                rowItem.orig.selected = isSelected;
            }
            if (rowItem.clone) {
                rowItem.clone.selected = isSelected;
            }
            rowItem.afterSelectionChange(rowItem);
        }
    };

    // @return - boolean indicating if all items are selected or not
    // @val - boolean indicating whether to select all/de-select all
    self.toggleSelectAll = function (checkAll, bypass, selectFiltered) {
        var rows = selectFiltered ? grid.filteredRows : grid.rowCache;
        if (bypass || grid.config.beforeSelectionChange(rows, checkAll)) {
            var selectedlength = self.selectedItems.length;
            if (selectedlength > 0) {
                self.selectedItems.length = 0;
            }
            for (var i = 0; i < rows.length; i++) {
                rows[i].selected = checkAll;
                if (rows[i].clone) {
                    rows[i].clone.selected = checkAll;
                }
                if (checkAll) {
                    self.selectedItems.push(rows[i].entity);
                }
            }
            if (!bypass) {
                grid.config.afterSelectionChange(rows, checkAll);
            }
        }
    };
};
var ngStyleProvider = function($scope, grid) {
    $scope.headerCellStyle = function(col) {
        return { "height": col.headerRowHeight + "px" };
    };
    $scope.rowStyle = function (row) {
        var ret = { "top": row.offsetTop + "px", "height": $scope.rowHeight + "px" };
        if (row.isAggRow) {
            ret.left = row.offsetLeft;
        }
        return ret;
    };
    $scope.canvasStyle = function() {
        return { "height": grid.maxCanvasHt + "px" };
    };
    $scope.headerScrollerStyle = function() {
        return { "height": grid.config.headerRowHeight + "px" };
    };
    $scope.topPanelStyle = function() {
        return { "width": grid.rootDim.outerWidth + "px", "height": $scope.topPanelHeight() + "px" };
    };
    $scope.headerStyle = function() {
        return { "width": grid.rootDim.outerWidth + "px", "height": grid.config.headerRowHeight + "px" };
    };
    $scope.groupPanelStyle = function () {
        return { "width": grid.rootDim.outerWidth + "px", "height": "32px" };
    };
    $scope.viewportStyle = function() {
        return { "width": grid.rootDim.outerWidth + "px", "height": $scope.viewportDimHeight() + "px" };
    };
    $scope.footerStyle = function() {
        return { "width": grid.rootDim.outerWidth + "px", "height": $scope.footerRowHeight + "px" };
    };
};
ngGridDirectives.directive('ngCellHasFocus', ['$domUtilityService',
    function (domUtilityService) {
        var focusOnInputElement = function($scope, elm) {
            $scope.isFocused = true;
            domUtilityService.digest($scope);

            $scope.$broadcast('ngGridEventStartCellEdit');

            $scope.$on('ngGridEventEndCellEdit', function() {
                $scope.isFocused = false;
                domUtilityService.digest($scope);
            });
        };

        return function($scope, elm) {
            var isFocused = false;
            var isCellEditableOnMouseDown = false;

            $scope.editCell = function() {
                if(!$scope.enableCellEditOnFocus) {
                    setTimeout(function() {
                        focusOnInputElement($scope,elm);
                    }, 0);
                }
            };
            elm.bind('mousedown', function(evt) {
                if($scope.enableCellEditOnFocus) {
                    isCellEditableOnMouseDown = true;
                } else {
                    elm.focus();
                }
                return true;
            });
            elm.bind('click', function(evt) {
                if($scope.enableCellEditOnFocus) {
                    evt.preventDefault();
                    isCellEditableOnMouseDown = false;
                    focusOnInputElement($scope,elm);
                }
            }); 
            elm.bind('focus', function(evt) {
                isFocused = true;
                if($scope.enableCellEditOnFocus && !isCellEditableOnMouseDown) {
                    focusOnInputElement($scope,elm);
                }
                return true;
            });
            elm.bind('blur', function() {
                isFocused = false;
                return true;
            });
            elm.bind('keydown', function(evt) {
                if(!$scope.enableCellEditOnFocus) {
                    if (isFocused && evt.keyCode !== 37 && evt.keyCode !== 38 && evt.keyCode !== 39 && evt.keyCode !== 40 && evt.keyCode !== 9 && !evt.shiftKey && evt.keyCode !== 13) {
                        focusOnInputElement($scope,elm);
                    }
                    if (isFocused && evt.shiftKey && (evt.keyCode >= 65 && evt.keyCode <= 90)) {
                        focusOnInputElement($scope, elm);
                    }
                    if (evt.keyCode === 27) {
                        elm.focus();
                    }
                }
                return true;
            });
        };
    }]);
ngGridDirectives.directive('ngCellText',
  function () {
      return function(scope, elm) {
          elm.bind('mouseover', function(evt) {
              evt.preventDefault();
              elm.css({
                  'cursor': 'text'
              });
          });
          elm.bind('mouseleave', function(evt) {
              evt.preventDefault();
              elm.css({
                  'cursor': 'default'
              });
          });
      };
  });
ngGridDirectives.directive('ngCell', ['$compile', '$domUtilityService', function ($compile, domUtilityService) {
    var ngCell = {
        scope: false,
        compile: function() {
            return {
                pre: function($scope, iElement) {
                    var html;
                    var cellTemplate = $scope.col.cellTemplate.replace(COL_FIELD, 'row.entity.' + $scope.col.field);

                    if ($scope.col.enableCellEdit) {
                        html =  $scope.col.cellEditTemplate;
                        html = html.replace(DISPLAY_CELL_TEMPLATE, cellTemplate);
                        html = html.replace(EDITABLE_CELL_TEMPLATE, $scope.col.editableCellTemplate.replace(COL_FIELD, 'row.entity.' + $scope.col.field));
                    } else {
                        html = cellTemplate;
                    }

                    var cellElement = $compile(html)($scope);

                    if ($scope.enableCellSelection && cellElement[0].className.indexOf('ngSelectionCell') === -1) {
                        cellElement[0].setAttribute('tabindex', 0);
                        cellElement.addClass('ngCellElement');
                    }

                    iElement.append(cellElement);
                },
                post: function($scope, iElement) {
                    if ($scope.enableCellSelection) {
                        $scope.domAccessProvider.selectionHandlers($scope, iElement);
                    }
                    
                    $scope.$on('ngGridEventDigestCell', function() {
                        domUtilityService.digest($scope);
                    });
                }
            };
        }
    };
    
    return ngCell;
}]);
/*
 * Defines the ui-if tag. This removes/adds an element from the dom depending on a condition
 * Originally created by @tigbro, for the @jquery-mobile-angular-adapter
 * https://github.com/tigbro/jquery-mobile-angular-adapter
 */
ngGridDirectives.directive('ngEditCellIf', [function () {
  return {
    transclude: 'element',
    priority: 1000,
    terminal: true,
    restrict: 'A',
    compile: function (e, a, transclude) {
      return function (scope, element, attr) {

        var childElement;
        var childScope;
 
        scope.$watch(attr['ngEditCellIf'], function (newValue) {
          if (childElement) {
            childElement.remove();
            childElement = undefined;
          }
          if (childScope) {
            childScope.$destroy();
            childScope = undefined;
          }

          if (newValue) {
            childScope = scope.$new();
            transclude(childScope, function (clone) {
              childElement = clone;
              element.after(clone);
            });
          }
        });
      };
    }
  };
}]);
ngGridDirectives.directive('ngGridFooter', ['$compile', '$templateCache', function ($compile, $templateCache) {
    var ngGridFooter = {
        scope: false,
        compile: function () {
            return {
                pre: function ($scope, iElement) {
                    if (iElement.children().length === 0) {
                        iElement.append($compile($templateCache.get($scope.gridId + 'footerTemplate.html'))($scope));
                    }
                }
            };
        }
    };
    return ngGridFooter;
}]);
ngGridDirectives.directive('ngGridMenu', ['$compile', '$templateCache', function ($compile, $templateCache) {
    var ngGridMenu = {
        scope: false,
        compile: function () {
            return {
                pre: function ($scope, iElement) {
                    if (iElement.children().length === 0) {
                        iElement.append($compile($templateCache.get($scope.gridId + 'menuTemplate.html'))($scope));
                    }
                }
            };
        }
    };
    return ngGridMenu;
}]);
ngGridDirectives.directive('ngGrid', ['$compile', '$filter', '$templateCache', '$sortService', '$domUtilityService', '$utilityService', '$timeout', '$parse', '$http', '$q', function ($compile, $filter, $templateCache, sortService, domUtilityService, $utils, $timeout, $parse, $http, $q) {
    var ngGridDirective = {
        scope: true,
        compile: function() {
            return {
                pre: function($scope, iElement, iAttrs) {
                    var $element = $(iElement);
                    var options = $scope.$eval(iAttrs.ngGrid);
                    options.gridDim = new ngDimension({ outerHeight: $($element).height(), outerWidth: $($element).width() });

                    var grid = new ngGrid($scope, options, sortService, domUtilityService, $filter, $templateCache, $utils, $timeout, $parse, $http, $q);
                    return grid.init().then(function() {
                        // if columndefs are a string of a property ont he scope watch for changes and rebuild columns.
                        if (typeof options.columnDefs === "string") {
                            $scope.$parent.$watch(options.columnDefs, function (a) {
                                if (!a) {
                                    grid.refreshDomSizes();
                                    grid.buildColumns();
                                    return;
                                }
                                // we have to set this to false in case we want to autogenerate columns with no initial data.
                                grid.lateBoundColumns = false;
                                $scope.columns = [];
                                grid.config.columnDefs = a;
                                grid.buildColumns();
                                grid.eventProvider.assignEvents();
                                domUtilityService.RebuildGrid($scope, grid);
                            }, true);
                        }
                        else {
                            grid.buildColumns();
                        }

                        // Watch totalServerItems if it's a string
                        if (typeof options.totalServerItems === "string") {
                            $scope.$parent.$watch(options.totalServerItems, function (newTotal, oldTotal) {
                                // If the newTotal is not defined (like during init, set the value to 0)
                                if (!angular.isDefined(newTotal)) {
                                    $scope.totalServerItems = 0;
                                }
                                // Otherwise set the value to the new total
                                else {
                                    $scope.totalServerItems = newTotal;
                                }
                            });
                        }
                        // If it's NOT a string, then just set totalServerItems to 0 since they should only be setting this if using a string
                        else {
                            $scope.totalServerItems = 0;
                        }
                        
                        // if it is a string we can watch for data changes. otherwise you won't be able to update the grid data
                        if (typeof options.data === "string") {
                            var dataWatcher = function (a) {
                                // make a temporary copy of the data
                                grid.data = $.extend([], a);
                                grid.rowFactory.fixRowCache();
                                angular.forEach(grid.data, function (item, j) {
                                    var indx = grid.rowMap[j] || j;
                                    if (grid.rowCache[indx]) {
                                        grid.rowCache[indx].ensureEntity(item);
                                    }
                                    grid.rowMap[indx] = j;
                                });
                                grid.searchProvider.evalFilter();
                                grid.configureColumnWidths();
                                grid.refreshDomSizes();
                                if (grid.config.sortInfo.fields.length > 0) {
                                    grid.sortColumnsInit();
                                    $scope.$emit('ngGridEventSorted', grid.config.sortInfo);
                                }
                                $scope.$emit("ngGridEventData", grid.gridId);
                            };
                            $scope.$parent.$watch(options.data, dataWatcher);
                            $scope.$parent.$watch(options.data + '.length', function() {
                                dataWatcher($scope.$eval(options.data));
                            });
                        }
                        
                        grid.footerController = new ngFooter($scope, grid);
                        //set the right styling on the container
                        iElement.addClass("ngGrid").addClass(grid.gridId.toString());
                        if (!options.enableHighlighting) {
                            iElement.addClass("unselectable");
                        }
                        if (options.jqueryUITheme) {
                            iElement.addClass('ui-widget');
                        }
                        iElement.append($compile($templateCache.get('gridTemplate.html'))($scope)); // make sure that if any of these change, we re-fire the calc logic
                        //walk the element's graph and the correct properties on the grid
                        domUtilityService.AssignGridContainers($scope, iElement, grid);
                        //now use the manager to assign the event handlers
                        grid.eventProvider = new ngEventProvider(grid, $scope, domUtilityService, $timeout);

                        // method for user to select a specific row programatically
                        options.selectRow = function (rowIndex, state) {
                            if (grid.rowCache[rowIndex]) {
                                if (grid.rowCache[rowIndex].clone) {
                                    grid.rowCache[rowIndex].clone.setSelection(state ? true : false);
                                } 
                                grid.rowCache[rowIndex].setSelection(state ? true : false);
                            }
                        };
                        // method for user to select the row by data item programatically
                        options.selectItem = function (itemIndex, state) {
                            options.selectRow(grid.rowMap[itemIndex], state);
                        };
                        // method for user to set the select all state.
                        options.selectAll = function (state) {
                            $scope.toggleSelectAll(state);
                        };
                        // method for user to set the select all state on visible items.
                        options.selectVisible = function (state) {
                            $scope.toggleSelectAll(state, true);
                        };
                        // method for user to set the groups programatically
                        options.groupBy = function (field) {
                            if (field) {
                                $scope.groupBy($scope.columns.filter(function(c) {
                                    return c.field === field;
                                })[0]);
                            } else {
                                var arr = $.extend(true, [], $scope.configGroups);
                                angular.forEach(arr, $scope.groupBy);
                            }
                        };
                        // method for user to set the sort field programatically
                        options.sortBy = function (field) {
                            var col = $scope.columns.filter(function (c) {
                                return c.field === field;
                            })[0];
                            if (col) {
                                col.sort();
                            }
                        };
                        // the grid Id, entity, scope for convenience
                        options.gridId = grid.gridId;
                        options.ngGrid = grid;
                        options.$gridScope = $scope;
                        options.$gridServices = { SortService: sortService, DomUtilityService: domUtilityService, UtilityService: $utils };
                        $scope.$on('ngGridEventDigestGrid', function(){
                            domUtilityService.digest($scope.$parent);
                        });         
                        
                        $scope.$on('ngGridEventDigestGridParent', function(){
                            domUtilityService.digest($scope.$parent);
                        });
                        // set up the columns 
                        $scope.$evalAsync(function() {
                            $scope.adjustScrollLeft(0);
                        });
                        //initialize plugins.
                        angular.forEach(options.plugins, function (p) {
                            if (typeof p === "function") {
                                p = new p(); //If p is a function, then we assume it is a class.
                            }
                            p.init($scope.$new(), grid, options.$gridServices);
                            options.plugins[$utils.getInstanceType(p)] = p;
                        });
                        //send initi finalize notification.
                        if (typeof options.init === "function") {
                            options.init(grid, $scope);
                        }
                        return null;
                    });
                }
            };
        }
    };
    return ngGridDirective;
}]);

ngGridDirectives.directive('ngHeaderCell', ['$compile', function($compile) {
    var ngHeaderCell = {
        scope: false,
        compile: function() {
            return {
                pre: function($scope, iElement) {
                    iElement.append($compile($scope.col.headerCellTemplate)($scope));
                }
            };
        }
    };
    return ngHeaderCell;
}]);
ngGridDirectives.directive('ngInput', [function() {
    return {
        require: 'ngModel',
        link: function (scope, elm, attrs, ngModel) {
            // Store the initial cell value so we can reset to it if need be
            var oldCellValue;
            var dereg = scope.$watch('ngModel', function() {
                oldCellValue = ngModel.$modelValue;
                dereg(); // only run this watch once, we don't want to overwrite our stored value when the input changes
            });
            
            elm.bind('keydown', function(evt) {
                switch (evt.keyCode) {
                    case 37: // Left arrow
                    case 38: // Up arrow
                    case 39: // Right arrow
                    case 40: // Down arrow
                        evt.stopPropagation();
                        break;
                    case 27: // Esc (reset to old value)
                        if (!scope.$$phase) {
                            scope.$apply(function() {
                                ngModel.$setViewValue(oldCellValue);
                                elm.blur();
                            });
                        }
                        break;
                    case 13: // Enter (Leave Field)
                        if(scope.enableCellEditOnFocus && scope.totalFilteredItemsLength() - 1 > scope.row.rowIndex && scope.row.rowIndex > 0  || scope.enableCellEdit) {
                            elm.blur();
                        }
                        break;
                }

                return true;
            });

            elm.bind('click', function(evt) {
                evt.stopPropagation();
            }); 

            elm.bind('mousedown', function(evt) {
                evt.stopPropagation();
            }); 

            scope.$on('ngGridEventStartCellEdit', function () {
                elm.focus();
                elm.select();
            });

            angular.element(elm).bind('blur', function () {
                scope.$emit('ngGridEventEndCellEdit');
            });
        }
    };
}]);
ngGridDirectives.directive('ngRow', ['$compile', '$domUtilityService', '$templateCache', function ($compile, domUtilityService, $templateCache) {
    var ngRow = {
        scope: false,
        compile: function() {
            return {
                pre: function($scope, iElement) {
                    $scope.row.elm = iElement;
                    if ($scope.row.clone) {
                        $scope.row.clone.elm = iElement;
                    }
                    if ($scope.row.isAggRow) {
                        var html = $templateCache.get($scope.gridId + 'aggregateTemplate.html');
                        if ($scope.row.aggLabelFilter) {
                            html = html.replace(CUSTOM_FILTERS, '| ' + $scope.row.aggLabelFilter);
                        } else {
                            html = html.replace(CUSTOM_FILTERS, "");
                        }
                        iElement.append($compile(html)($scope));
                    } else {
                        iElement.append($compile($templateCache.get($scope.gridId + 'rowTemplate.html'))($scope));
                    }
					$scope.$on('ngGridEventDigestRow', function(){
						domUtilityService.digest($scope);
					});
                }
            };
        }
    };
    return ngRow;
}]);
ngGridDirectives.directive('ngViewport', [function() {
    return function($scope, elm) {
        var isMouseWheelActive;
        var prevScollLeft;
        var prevScollTop = 0;
        elm.bind('scroll', function(evt) {
            var scrollLeft = evt.target.scrollLeft,
                scrollTop = evt.target.scrollTop;
            if ($scope.$headerContainer) {
                $scope.$headerContainer.scrollLeft(scrollLeft);
            }
            $scope.adjustScrollLeft(scrollLeft);
            $scope.adjustScrollTop(scrollTop);
            if (!$scope.$root.$$phase) {
                $scope.$digest();
            }
            prevScollLeft = scrollLeft;
            prevScollTop = scrollTop;
            isMouseWheelActive = false;
            return true;
        });
        elm.bind("mousewheel DOMMouseScroll", function() {
            isMouseWheelActive = true;
            if (elm.focus) { elm.focus(); }
            return true;
        });
        if (!$scope.enableCellSelection) {
            $scope.domAccessProvider.selectionHandlers($scope, elm);
        }
    };
}]);
window.ngGrid.i18n['da'] = {
    ngAggregateLabel: 'artikler',
    ngGroupPanelDescription: 'Grupér rækker udfra en kolonne ved at trække dens overskift hertil.',
    ngSearchPlaceHolder: 'Søg...',
    ngMenuText: 'Vælg kolonner:',
    ngShowingItemsLabel: 'Viste rækker:',
    ngTotalItemsLabel: 'Rækker totalt:',
    ngSelectedItemsLabel: 'Valgte rækker:',
    ngPageSizeLabel: 'Side størrelse:',
    ngPagerFirstTitle: 'Første side',
    ngPagerNextTitle: 'Næste side',
    ngPagerPrevTitle: 'Forrige side',
    ngPagerLastTitle: 'Sidste side'
};
window.ngGrid.i18n['de'] = {
    ngAggregateLabel: 'artikel',
    ngGroupPanelDescription: 'Ziehen Sie eine Spaltenüberschrift hier und legen Sie es der Gruppe nach dieser Spalte.',
    ngSearchPlaceHolder: 'Suche...',
    ngMenuText: 'Spalten auswählen:',
    ngShowingItemsLabel: 'Zeige Artikel:',
    ngTotalItemsLabel: 'Meiste Artikel:',
    ngSelectedItemsLabel: 'Ausgewählte Artikel:',
    ngPageSizeLabel: 'Größe Seite:',
    ngPagerFirstTitle: 'Erste Page',
    ngPagerNextTitle: 'Nächste Page',
    ngPagerPrevTitle: 'Vorherige Page',
    ngPagerLastTitle: 'Letzte Page'
};
window.ngGrid.i18n['en'] = {
    ngAggregateLabel: 'items',
    ngGroupPanelDescription: 'Drag a column header here and drop it to group by that column.',
    ngSearchPlaceHolder: 'Search...',
    ngMenuText: 'Choose Columns:',
    ngShowingItemsLabel: 'Showing Items:',
    ngTotalItemsLabel: 'Total Items:',
    ngSelectedItemsLabel: 'Selected Items:',
    ngPageSizeLabel: 'Page Size:',
    ngPagerFirstTitle: 'First Page',
    ngPagerNextTitle: 'Next Page',
    ngPagerPrevTitle: 'Previous Page',
    ngPagerLastTitle: 'Last Page'
};
window.ngGrid.i18n['es'] = {
    ngAggregateLabel: 'Artículos',
    ngGroupPanelDescription: 'Arrastre un encabezado de columna aquí y soltarlo para agrupar por esa columna.',
    ngSearchPlaceHolder: 'Buscar...',
    ngMenuText: 'Elegir columnas:',
    ngShowingItemsLabel: 'Artículos Mostrando:',
    ngTotalItemsLabel: 'Artículos Totales:',
    ngSelectedItemsLabel: 'Artículos Seleccionados:',
    ngPageSizeLabel: 'Tamaño de Página:',
    ngPagerFirstTitle: 'Primera Página',
    ngPagerNextTitle: 'Página Siguiente',
    ngPagerPrevTitle: 'Página Anterior',
    ngPagerLastTitle: 'Última Página'
};
window.ngGrid.i18n['fr'] = {
    ngAggregateLabel: 'articles',
    ngGroupPanelDescription: 'Faites glisser un en-tête de colonne ici et déposez-le vers un groupe par cette colonne.',
    ngSearchPlaceHolder: 'Recherche...',
    ngMenuText: 'Choisir des colonnes:',
    ngShowingItemsLabel: 'Articles Affichage des:',
    ngTotalItemsLabel: 'Nombre total d\'articles:',
    ngSelectedItemsLabel: 'Éléments Articles:',
    ngPageSizeLabel: 'Taille de page:',
    ngPagerFirstTitle: 'Première page',
    ngPagerNextTitle: 'Page Suivante',
    ngPagerPrevTitle: 'Page précédente',
    ngPagerLastTitle: 'Dernière page'
};
window.ngGrid.i18n['pt-br'] = {
    ngAggregateLabel: 'items',
    ngGroupPanelDescription: 'Arraste e solte uma coluna aqui para agrupar por essa coluna',
    ngSearchPlaceHolder: 'Procurar...',
    ngMenuText: 'Selecione as colunas:',
    ngShowingItemsLabel: 'Mostrando os Items:',
    ngTotalItemsLabel: 'Total de Items:',
    ngSelectedItemsLabel: 'Items Selecionados:',
    ngPageSizeLabel: 'Tamanho da Página:',
    ngPagerFirstTitle: 'Primeira Página',
    ngPagerNextTitle: 'Próxima Página',
    ngPagerPrevTitle: 'Página Anterior',
    ngPagerLastTitle: 'Última Página'
};
window.ngGrid.i18n['zh-cn'] = {
    ngAggregateLabel: '条目',
    ngGroupPanelDescription: '拖曳表头到此处以进行分组',
    ngSearchPlaceHolder: '搜索...',
    ngMenuText: '数据分组与选择列：',
    ngShowingItemsLabel: '当前显示条目：',
    ngTotalItemsLabel: '条目总数：',
    ngSelectedItemsLabel: '选中条目：',
    ngPageSizeLabel: '每页显示数：',
    ngPagerFirstTitle: '回到首页',
    ngPagerNextTitle: '下一页',
    ngPagerPrevTitle: '上一页',
    ngPagerLastTitle: '前往尾页' 
};

window.ngGrid.i18n['zh-tw'] = {
    ngAggregateLabel: '筆',
    ngGroupPanelDescription: '拖拉表頭到此處以進行分組',
    ngSearchPlaceHolder: '搜尋...',
    ngMenuText: '選擇欄位：',
    ngShowingItemsLabel: '目前顯示筆數：',
    ngTotalItemsLabel: '總筆數：',
    ngSelectedItemsLabel: '選取筆數：',
    ngPageSizeLabel: '每頁顯示：',
    ngPagerFirstTitle: '第一頁',
    ngPagerNextTitle: '下一頁',
    ngPagerPrevTitle: '上一頁',
    ngPagerLastTitle: '最後頁'
};

angular.module("ngGrid").run(["$templateCache", function($templateCache) {

  $templateCache.put("aggregateTemplate.html",
    "<div ng-click=\"row.toggleExpand()\" ng-style=\"rowStyle(row)\" class=\"ngAggregate\">\n" +
    "    <span class=\"ngAggregateText\">{{row.label CUSTOM_FILTERS}} ({{row.totalChildren()}} {{AggItemsLabel}})</span>\n" +
    "    <div class=\"{{row.aggClass()}}\"></div>\n" +
    "</div>\n"
  );

  $templateCache.put("cellEditTemplate.html",
    "<div ng-cell-has-focus ng-dblclick=\"editCell()\">\n" +
    "\t<div ng-edit-cell-if=\"!isFocused\">\t\n" +
    "\t\tDISPLAY_CELL_TEMPLATE\n" +
    "\t</div>\n" +
    "\t<div ng-edit-cell-if=\"isFocused\">\n" +
    "\t\tEDITABLE_CELL_TEMPLATE\n" +
    "\t</div>\n" +
    "</div>"
  );

  $templateCache.put("cellTemplate.html",
    "<div class=\"ngCellText\" ng-class=\"col.colIndex()\"><span ng-cell-text>{{COL_FIELD CUSTOM_FILTERS}}</span></div>"
  );

  $templateCache.put("checkboxCellTemplate.html",
    "<div class=\"ngSelectionCell\"><input tabindex=\"-1\" class=\"ngSelectionCheckbox\" type=\"checkbox\" ng-checked=\"row.selected\" /></div>"
  );

  $templateCache.put("checkboxHeaderTemplate.html",
    "<input class=\"ngSelectionHeader\" type=\"checkbox\" ng-show=\"multiSelect\" ng-model=\"allSelected\" ng-change=\"toggleSelectAll(allSelected, true)\"/>"
  );

  $templateCache.put("editableCellTemplate.html",
    "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" />"
  );

  $templateCache.put("footerTemplate.html",
    "<div ng-show=\"showFooter\" class=\"ngFooterPanel\" ng-class=\"{'ui-widget-content': jqueryUITheme, 'ui-corner-bottom': jqueryUITheme}\" ng-style=\"footerStyle()\">\n" +
    "    <div class=\"ngTotalSelectContainer\" >\n" +
    "        <div class=\"ngFooterTotalItems\" ng-class=\"{'ngNoMultiSelect': !multiSelect}\" >\n" +
    "            <span class=\"ngLabel\">{{i18n.ngTotalItemsLabel}} {{maxRows()}}</span><span ng-show=\"filterText.length > 0\" class=\"ngLabel\">({{i18n.ngShowingItemsLabel}} {{totalFilteredItemsLength()}})</span>\n" +
    "        </div>\n" +
    "        <div class=\"ngFooterSelectedItems\" ng-show=\"multiSelect\">\n" +
    "            <span class=\"ngLabel\">{{i18n.ngSelectedItemsLabel}} {{selectedItems.length}}</span>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"ngPagerContainer\" style=\"float: right; margin-top: 10px;\" ng-show=\"enablePaging\" ng-class=\"{'ngNoMultiSelect': !multiSelect}\">\n" +
    "        <div style=\"float:left; margin-right: 10px;\" class=\"ngRowCountPicker\">\n" +
    "            <span style=\"float: left; margin-top: 3px;\" class=\"ngLabel\">{{i18n.ngPageSizeLabel}}</span>\n" +
    "            <select style=\"float: left;height: 27px; width: 100px\" ng-model=\"pagingOptions.pageSize\" >\n" +
    "                <option ng-repeat=\"size in pagingOptions.pageSizes\">{{size}}</option>\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <div style=\"float:left; margin-right: 10px; line-height:25px;\" class=\"ngPagerControl\" style=\"float: left; min-width: 135px;\">\n" +
    "            <button class=\"ngPagerButton\" ng-click=\"pageToFirst()\" ng-disabled=\"cantPageBackward()\" title=\"{{i18n.ngPagerFirstTitle}}\"><div class=\"ngPagerFirstTriangle\"><div class=\"ngPagerFirstBar\"></div></div></button>\n" +
    "            <button class=\"ngPagerButton\" ng-click=\"pageBackward()\" ng-disabled=\"cantPageBackward()\" title=\"{{i18n.ngPagerPrevTitle}}\"><div class=\"ngPagerFirstTriangle ngPagerPrevTriangle\"></div></button>\n" +
    "            <input class=\"ngPagerCurrent\" min=\"1\" max=\"{{maxPages()}}\" type=\"number\" style=\"width:50px; height: 24px; margin-top: 1px; padding: 0 4px;\" ng-model=\"pagingOptions.currentPage\"/>\n" +
    "            <button class=\"ngPagerButton\" ng-click=\"pageForward()\" ng-disabled=\"cantPageForward()\" title=\"{{i18n.ngPagerNextTitle}}\"><div class=\"ngPagerLastTriangle ngPagerNextTriangle\"></div></button>\n" +
    "            <button class=\"ngPagerButton\" ng-click=\"pageToLast()\" ng-disabled=\"cantPageToLast()\" title=\"{{i18n.ngPagerLastTitle}}\"><div class=\"ngPagerLastTriangle\"><div class=\"ngPagerLastBar\"></div></div></button>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>"
  );

  $templateCache.put("gridTemplate.html",
    "<div class=\"ngTopPanel\" ng-class=\"{'ui-widget-header':jqueryUITheme, 'ui-corner-top': jqueryUITheme}\" ng-style=\"topPanelStyle()\">\n" +
    "    <div class=\"ngGroupPanel\" ng-show=\"showGroupPanel()\" ng-style=\"groupPanelStyle()\">\n" +
    "        <div class=\"ngGroupPanelDescription\" ng-show=\"configGroups.length == 0\">{{i18n.ngGroupPanelDescription}}</div>\n" +
    "        <ul ng-show=\"configGroups.length > 0\" class=\"ngGroupList\">\n" +
    "            <li class=\"ngGroupItem\" ng-repeat=\"group in configGroups\">\n" +
    "                <span class=\"ngGroupElement\">\n" +
    "                    <span class=\"ngGroupName\">{{group.displayName}}\n" +
    "                        <span ng-click=\"removeGroup($index)\" class=\"ngRemoveGroup\">x</span>\n" +
    "                    </span>\n" +
    "                    <span ng-hide=\"$last\" class=\"ngGroupArrow\"></span>\n" +
    "                </span>\n" +
    "            </li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "    <div class=\"ngHeaderContainer\" ng-style=\"headerStyle()\">\n" +
    "        <div class=\"ngHeaderScroller\" ng-style=\"headerScrollerStyle()\" ng-include=\"gridId + 'headerRowTemplate.html'\"></div>\n" +
    "    </div>\n" +
    "    <div ng-grid-menu></div>\n" +
    "</div>\n" +
    "<div class=\"ngViewport\" unselectable=\"on\" ng-viewport ng-class=\"{'ui-widget-content': jqueryUITheme}\" ng-style=\"viewportStyle()\">\n" +
    "    <div class=\"ngCanvas\" ng-style=\"canvasStyle()\">\n" +
    "        <div ng-style=\"rowStyle(row)\" ng-repeat=\"row in renderedRows\" ng-click=\"row.toggleSelected($event)\" ng-class=\"row.alternatingRowClass()\" ng-row></div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<div ng-grid-footer></div>\n"
  );

  $templateCache.put("headerCellTemplate.html",
    "<div class=\"ngHeaderSortColumn {{col.headerClass}}\" ng-style=\"{'cursor': col.cursor}\" ng-class=\"{ 'ngSorted': !noSortVisible }\">\n" +
    "    <div ng-click=\"col.sort($event)\" ng-class=\"'colt' + col.index\" class=\"ngHeaderText\">{{col.displayName}}</div>\n" +
    "    <div class=\"ngSortButtonDown\" ng-show=\"col.showSortButtonDown()\"></div>\n" +
    "    <div class=\"ngSortButtonUp\" ng-show=\"col.showSortButtonUp()\"></div>\n" +
    "    <div class=\"ngSortPriority\">{{col.sortPriority}}</div>\n" +
    "    <div ng-class=\"{ ngPinnedIcon: col.pinned, ngUnPinnedIcon: !col.pinned }\" ng-click=\"togglePin(col)\" ng-show=\"col.pinnable\"></div>\n" +
    "</div>\n" +
    "<div ng-show=\"col.resizable\" class=\"ngHeaderGrip\" ng-click=\"col.gripClick($event)\" ng-mousedown=\"col.gripOnMouseDown($event)\"></div>"
  );

  $templateCache.put("headerRowTemplate.html",
    "<div ng-style=\"{ height: col.headerRowHeight }\" ng-repeat=\"col in renderedColumns\" ng-class=\"col.colIndex()\" class=\"ngHeaderCell\">\n" +
    "\t<div class=\"ngVerticalBar\" ng-style=\"{height: col.headerRowHeight}\" ng-class=\"{ ngVerticalBarVisible: !$last }\">&nbsp;</div>\n" +
    "\t<div ng-header-cell></div>\n" +
    "</div>"
  );

  $templateCache.put("menuTemplate.html",
    "<div ng-show=\"showColumnMenu || showFilter\"  class=\"ngHeaderButton\" ng-click=\"toggleShowMenu()\">\n" +
    "    <div class=\"ngHeaderButtonArrow\"></div>\n" +
    "</div>\n" +
    "<div ng-show=\"showMenu\" class=\"ngColMenu\">\n" +
    "    <div ng-show=\"showFilter\">\n" +
    "        <input placeholder=\"{{i18n.ngSearchPlaceHolder}}\" type=\"text\" ng-model=\"filterText\"/>\n" +
    "    </div>\n" +
    "    <div ng-show=\"showColumnMenu\">\n" +
    "        <span class=\"ngMenuText\">{{i18n.ngMenuText}}</span>\n" +
    "        <ul class=\"ngColList\">\n" +
    "            <li class=\"ngColListItem\" ng-repeat=\"col in columns | ngColumns\">\n" +
    "                <label><input ng-disabled=\"col.pinned\" type=\"checkbox\" class=\"ngColListCheckbox\" ng-model=\"col.visible\"/>{{col.displayName}}</label>\n" +
    "\t\t\t\t<a title=\"Group By\" ng-class=\"col.groupedByClass()\" ng-show=\"col.groupable && col.visible\" ng-click=\"groupBy(col)\"></a>\n" +
    "\t\t\t\t<span class=\"ngGroupingNumber\" ng-show=\"col.groupIndex > 0\">{{col.groupIndex}}</span>          \n" +
    "            </li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "</div>"
  );

  $templateCache.put("rowTemplate.html",
    "<div ng-style=\"{ 'cursor': row.cursor }\" ng-repeat=\"col in renderedColumns\" ng-class=\"col.colIndex()\" class=\"ngCell {{col.cellClass}}\">\n" +
    "\t<div class=\"ngVerticalBar\" ng-style=\"{height: rowHeight}\" ng-class=\"{ ngVerticalBarVisible: !$last }\">&nbsp;</div>\n" +
    "\t<div ng-cell></div>\n" +
    "</div>"
  );

}]);

}(window, jQuery));
/*
Copyright 2012 Igor Vaynberg

Version: 3.4.8 Timestamp: Thu May  1 09:50:32 EDT 2014

This software is licensed under the Apache License, Version 2.0 (the "Apache License") or the GNU
General Public License version 2 (the "GPL License"). You may choose either license to govern your
use of this software only upon the condition that you accept all of the terms of either the Apache
License or the GPL License.

You may obtain a copy of the Apache License and the GPL License at:

    http://www.apache.org/licenses/LICENSE-2.0
    http://www.gnu.org/licenses/gpl-2.0.html

Unless required by applicable law or agreed to in writing, software distributed under the
Apache License or the GPL License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the Apache License and the GPL License for
the specific language governing permissions and limitations under the Apache License and the GPL License.
*/
(function ($) {
    if(typeof $.fn.each2 == "undefined") {
        $.extend($.fn, {
            /*
            * 4-10 times faster .each replacement
            * use it carefully, as it overrides jQuery context of element on each iteration
            */
            each2 : function (c) {
                var j = $([0]), i = -1, l = this.length;
                while (
                    ++i < l
                    && (j.context = j[0] = this[i])
                    && c.call(j[0], i, j) !== false //"this"=DOM, i=index, j=jQuery object
                );
                return this;
            }
        });
    }
})(jQuery);

(function ($, undefined) {
    "use strict";
    /*global document, window, jQuery, console */

    if (window.Select2 !== undefined) {
        return;
    }

    var KEY, AbstractSelect2, SingleSelect2, MultiSelect2, nextUid, sizer,
        lastMousePosition={x:0,y:0}, $document, scrollBarDimensions,

    KEY = {
        TAB: 9,
        ENTER: 13,
        ESC: 27,
        SPACE: 32,
        LEFT: 37,
        UP: 38,
        RIGHT: 39,
        DOWN: 40,
        SHIFT: 16,
        CTRL: 17,
        ALT: 18,
        PAGE_UP: 33,
        PAGE_DOWN: 34,
        HOME: 36,
        END: 35,
        BACKSPACE: 8,
        DELETE: 46,
        isArrow: function (k) {
            k = k.which ? k.which : k;
            switch (k) {
            case KEY.LEFT:
            case KEY.RIGHT:
            case KEY.UP:
            case KEY.DOWN:
                return true;
            }
            return false;
        },
        isControl: function (e) {
            var k = e.which;
            switch (k) {
            case KEY.SHIFT:
            case KEY.CTRL:
            case KEY.ALT:
                return true;
            }

            if (e.metaKey) return true;

            return false;
        },
        isFunctionKey: function (k) {
            k = k.which ? k.which : k;
            return k >= 112 && k <= 123;
        }
    },
    MEASURE_SCROLLBAR_TEMPLATE = "<div class='select2-measure-scrollbar'></div>",

    DIACRITICS = {"\u24B6":"A","\uFF21":"A","\u00C0":"A","\u00C1":"A","\u00C2":"A","\u1EA6":"A","\u1EA4":"A","\u1EAA":"A","\u1EA8":"A","\u00C3":"A","\u0100":"A","\u0102":"A","\u1EB0":"A","\u1EAE":"A","\u1EB4":"A","\u1EB2":"A","\u0226":"A","\u01E0":"A","\u00C4":"A","\u01DE":"A","\u1EA2":"A","\u00C5":"A","\u01FA":"A","\u01CD":"A","\u0200":"A","\u0202":"A","\u1EA0":"A","\u1EAC":"A","\u1EB6":"A","\u1E00":"A","\u0104":"A","\u023A":"A","\u2C6F":"A","\uA732":"AA","\u00C6":"AE","\u01FC":"AE","\u01E2":"AE","\uA734":"AO","\uA736":"AU","\uA738":"AV","\uA73A":"AV","\uA73C":"AY","\u24B7":"B","\uFF22":"B","\u1E02":"B","\u1E04":"B","\u1E06":"B","\u0243":"B","\u0182":"B","\u0181":"B","\u24B8":"C","\uFF23":"C","\u0106":"C","\u0108":"C","\u010A":"C","\u010C":"C","\u00C7":"C","\u1E08":"C","\u0187":"C","\u023B":"C","\uA73E":"C","\u24B9":"D","\uFF24":"D","\u1E0A":"D","\u010E":"D","\u1E0C":"D","\u1E10":"D","\u1E12":"D","\u1E0E":"D","\u0110":"D","\u018B":"D","\u018A":"D","\u0189":"D","\uA779":"D","\u01F1":"DZ","\u01C4":"DZ","\u01F2":"Dz","\u01C5":"Dz","\u24BA":"E","\uFF25":"E","\u00C8":"E","\u00C9":"E","\u00CA":"E","\u1EC0":"E","\u1EBE":"E","\u1EC4":"E","\u1EC2":"E","\u1EBC":"E","\u0112":"E","\u1E14":"E","\u1E16":"E","\u0114":"E","\u0116":"E","\u00CB":"E","\u1EBA":"E","\u011A":"E","\u0204":"E","\u0206":"E","\u1EB8":"E","\u1EC6":"E","\u0228":"E","\u1E1C":"E","\u0118":"E","\u1E18":"E","\u1E1A":"E","\u0190":"E","\u018E":"E","\u24BB":"F","\uFF26":"F","\u1E1E":"F","\u0191":"F","\uA77B":"F","\u24BC":"G","\uFF27":"G","\u01F4":"G","\u011C":"G","\u1E20":"G","\u011E":"G","\u0120":"G","\u01E6":"G","\u0122":"G","\u01E4":"G","\u0193":"G","\uA7A0":"G","\uA77D":"G","\uA77E":"G","\u24BD":"H","\uFF28":"H","\u0124":"H","\u1E22":"H","\u1E26":"H","\u021E":"H","\u1E24":"H","\u1E28":"H","\u1E2A":"H","\u0126":"H","\u2C67":"H","\u2C75":"H","\uA78D":"H","\u24BE":"I","\uFF29":"I","\u00CC":"I","\u00CD":"I","\u00CE":"I","\u0128":"I","\u012A":"I","\u012C":"I","\u0130":"I","\u00CF":"I","\u1E2E":"I","\u1EC8":"I","\u01CF":"I","\u0208":"I","\u020A":"I","\u1ECA":"I","\u012E":"I","\u1E2C":"I","\u0197":"I","\u24BF":"J","\uFF2A":"J","\u0134":"J","\u0248":"J","\u24C0":"K","\uFF2B":"K","\u1E30":"K","\u01E8":"K","\u1E32":"K","\u0136":"K","\u1E34":"K","\u0198":"K","\u2C69":"K","\uA740":"K","\uA742":"K","\uA744":"K","\uA7A2":"K","\u24C1":"L","\uFF2C":"L","\u013F":"L","\u0139":"L","\u013D":"L","\u1E36":"L","\u1E38":"L","\u013B":"L","\u1E3C":"L","\u1E3A":"L","\u0141":"L","\u023D":"L","\u2C62":"L","\u2C60":"L","\uA748":"L","\uA746":"L","\uA780":"L","\u01C7":"LJ","\u01C8":"Lj","\u24C2":"M","\uFF2D":"M","\u1E3E":"M","\u1E40":"M","\u1E42":"M","\u2C6E":"M","\u019C":"M","\u24C3":"N","\uFF2E":"N","\u01F8":"N","\u0143":"N","\u00D1":"N","\u1E44":"N","\u0147":"N","\u1E46":"N","\u0145":"N","\u1E4A":"N","\u1E48":"N","\u0220":"N","\u019D":"N","\uA790":"N","\uA7A4":"N","\u01CA":"NJ","\u01CB":"Nj","\u24C4":"O","\uFF2F":"O","\u00D2":"O","\u00D3":"O","\u00D4":"O","\u1ED2":"O","\u1ED0":"O","\u1ED6":"O","\u1ED4":"O","\u00D5":"O","\u1E4C":"O","\u022C":"O","\u1E4E":"O","\u014C":"O","\u1E50":"O","\u1E52":"O","\u014E":"O","\u022E":"O","\u0230":"O","\u00D6":"O","\u022A":"O","\u1ECE":"O","\u0150":"O","\u01D1":"O","\u020C":"O","\u020E":"O","\u01A0":"O","\u1EDC":"O","\u1EDA":"O","\u1EE0":"O","\u1EDE":"O","\u1EE2":"O","\u1ECC":"O","\u1ED8":"O","\u01EA":"O","\u01EC":"O","\u00D8":"O","\u01FE":"O","\u0186":"O","\u019F":"O","\uA74A":"O","\uA74C":"O","\u01A2":"OI","\uA74E":"OO","\u0222":"OU","\u24C5":"P","\uFF30":"P","\u1E54":"P","\u1E56":"P","\u01A4":"P","\u2C63":"P","\uA750":"P","\uA752":"P","\uA754":"P","\u24C6":"Q","\uFF31":"Q","\uA756":"Q","\uA758":"Q","\u024A":"Q","\u24C7":"R","\uFF32":"R","\u0154":"R","\u1E58":"R","\u0158":"R","\u0210":"R","\u0212":"R","\u1E5A":"R","\u1E5C":"R","\u0156":"R","\u1E5E":"R","\u024C":"R","\u2C64":"R","\uA75A":"R","\uA7A6":"R","\uA782":"R","\u24C8":"S","\uFF33":"S","\u1E9E":"S","\u015A":"S","\u1E64":"S","\u015C":"S","\u1E60":"S","\u0160":"S","\u1E66":"S","\u1E62":"S","\u1E68":"S","\u0218":"S","\u015E":"S","\u2C7E":"S","\uA7A8":"S","\uA784":"S","\u24C9":"T","\uFF34":"T","\u1E6A":"T","\u0164":"T","\u1E6C":"T","\u021A":"T","\u0162":"T","\u1E70":"T","\u1E6E":"T","\u0166":"T","\u01AC":"T","\u01AE":"T","\u023E":"T","\uA786":"T","\uA728":"TZ","\u24CA":"U","\uFF35":"U","\u00D9":"U","\u00DA":"U","\u00DB":"U","\u0168":"U","\u1E78":"U","\u016A":"U","\u1E7A":"U","\u016C":"U","\u00DC":"U","\u01DB":"U","\u01D7":"U","\u01D5":"U","\u01D9":"U","\u1EE6":"U","\u016E":"U","\u0170":"U","\u01D3":"U","\u0214":"U","\u0216":"U","\u01AF":"U","\u1EEA":"U","\u1EE8":"U","\u1EEE":"U","\u1EEC":"U","\u1EF0":"U","\u1EE4":"U","\u1E72":"U","\u0172":"U","\u1E76":"U","\u1E74":"U","\u0244":"U","\u24CB":"V","\uFF36":"V","\u1E7C":"V","\u1E7E":"V","\u01B2":"V","\uA75E":"V","\u0245":"V","\uA760":"VY","\u24CC":"W","\uFF37":"W","\u1E80":"W","\u1E82":"W","\u0174":"W","\u1E86":"W","\u1E84":"W","\u1E88":"W","\u2C72":"W","\u24CD":"X","\uFF38":"X","\u1E8A":"X","\u1E8C":"X","\u24CE":"Y","\uFF39":"Y","\u1EF2":"Y","\u00DD":"Y","\u0176":"Y","\u1EF8":"Y","\u0232":"Y","\u1E8E":"Y","\u0178":"Y","\u1EF6":"Y","\u1EF4":"Y","\u01B3":"Y","\u024E":"Y","\u1EFE":"Y","\u24CF":"Z","\uFF3A":"Z","\u0179":"Z","\u1E90":"Z","\u017B":"Z","\u017D":"Z","\u1E92":"Z","\u1E94":"Z","\u01B5":"Z","\u0224":"Z","\u2C7F":"Z","\u2C6B":"Z","\uA762":"Z","\u24D0":"a","\uFF41":"a","\u1E9A":"a","\u00E0":"a","\u00E1":"a","\u00E2":"a","\u1EA7":"a","\u1EA5":"a","\u1EAB":"a","\u1EA9":"a","\u00E3":"a","\u0101":"a","\u0103":"a","\u1EB1":"a","\u1EAF":"a","\u1EB5":"a","\u1EB3":"a","\u0227":"a","\u01E1":"a","\u00E4":"a","\u01DF":"a","\u1EA3":"a","\u00E5":"a","\u01FB":"a","\u01CE":"a","\u0201":"a","\u0203":"a","\u1EA1":"a","\u1EAD":"a","\u1EB7":"a","\u1E01":"a","\u0105":"a","\u2C65":"a","\u0250":"a","\uA733":"aa","\u00E6":"ae","\u01FD":"ae","\u01E3":"ae","\uA735":"ao","\uA737":"au","\uA739":"av","\uA73B":"av","\uA73D":"ay","\u24D1":"b","\uFF42":"b","\u1E03":"b","\u1E05":"b","\u1E07":"b","\u0180":"b","\u0183":"b","\u0253":"b","\u24D2":"c","\uFF43":"c","\u0107":"c","\u0109":"c","\u010B":"c","\u010D":"c","\u00E7":"c","\u1E09":"c","\u0188":"c","\u023C":"c","\uA73F":"c","\u2184":"c","\u24D3":"d","\uFF44":"d","\u1E0B":"d","\u010F":"d","\u1E0D":"d","\u1E11":"d","\u1E13":"d","\u1E0F":"d","\u0111":"d","\u018C":"d","\u0256":"d","\u0257":"d","\uA77A":"d","\u01F3":"dz","\u01C6":"dz","\u24D4":"e","\uFF45":"e","\u00E8":"e","\u00E9":"e","\u00EA":"e","\u1EC1":"e","\u1EBF":"e","\u1EC5":"e","\u1EC3":"e","\u1EBD":"e","\u0113":"e","\u1E15":"e","\u1E17":"e","\u0115":"e","\u0117":"e","\u00EB":"e","\u1EBB":"e","\u011B":"e","\u0205":"e","\u0207":"e","\u1EB9":"e","\u1EC7":"e","\u0229":"e","\u1E1D":"e","\u0119":"e","\u1E19":"e","\u1E1B":"e","\u0247":"e","\u025B":"e","\u01DD":"e","\u24D5":"f","\uFF46":"f","\u1E1F":"f","\u0192":"f","\uA77C":"f","\u24D6":"g","\uFF47":"g","\u01F5":"g","\u011D":"g","\u1E21":"g","\u011F":"g","\u0121":"g","\u01E7":"g","\u0123":"g","\u01E5":"g","\u0260":"g","\uA7A1":"g","\u1D79":"g","\uA77F":"g","\u24D7":"h","\uFF48":"h","\u0125":"h","\u1E23":"h","\u1E27":"h","\u021F":"h","\u1E25":"h","\u1E29":"h","\u1E2B":"h","\u1E96":"h","\u0127":"h","\u2C68":"h","\u2C76":"h","\u0265":"h","\u0195":"hv","\u24D8":"i","\uFF49":"i","\u00EC":"i","\u00ED":"i","\u00EE":"i","\u0129":"i","\u012B":"i","\u012D":"i","\u00EF":"i","\u1E2F":"i","\u1EC9":"i","\u01D0":"i","\u0209":"i","\u020B":"i","\u1ECB":"i","\u012F":"i","\u1E2D":"i","\u0268":"i","\u0131":"i","\u24D9":"j","\uFF4A":"j","\u0135":"j","\u01F0":"j","\u0249":"j","\u24DA":"k","\uFF4B":"k","\u1E31":"k","\u01E9":"k","\u1E33":"k","\u0137":"k","\u1E35":"k","\u0199":"k","\u2C6A":"k","\uA741":"k","\uA743":"k","\uA745":"k","\uA7A3":"k","\u24DB":"l","\uFF4C":"l","\u0140":"l","\u013A":"l","\u013E":"l","\u1E37":"l","\u1E39":"l","\u013C":"l","\u1E3D":"l","\u1E3B":"l","\u017F":"l","\u0142":"l","\u019A":"l","\u026B":"l","\u2C61":"l","\uA749":"l","\uA781":"l","\uA747":"l","\u01C9":"lj","\u24DC":"m","\uFF4D":"m","\u1E3F":"m","\u1E41":"m","\u1E43":"m","\u0271":"m","\u026F":"m","\u24DD":"n","\uFF4E":"n","\u01F9":"n","\u0144":"n","\u00F1":"n","\u1E45":"n","\u0148":"n","\u1E47":"n","\u0146":"n","\u1E4B":"n","\u1E49":"n","\u019E":"n","\u0272":"n","\u0149":"n","\uA791":"n","\uA7A5":"n","\u01CC":"nj","\u24DE":"o","\uFF4F":"o","\u00F2":"o","\u00F3":"o","\u00F4":"o","\u1ED3":"o","\u1ED1":"o","\u1ED7":"o","\u1ED5":"o","\u00F5":"o","\u1E4D":"o","\u022D":"o","\u1E4F":"o","\u014D":"o","\u1E51":"o","\u1E53":"o","\u014F":"o","\u022F":"o","\u0231":"o","\u00F6":"o","\u022B":"o","\u1ECF":"o","\u0151":"o","\u01D2":"o","\u020D":"o","\u020F":"o","\u01A1":"o","\u1EDD":"o","\u1EDB":"o","\u1EE1":"o","\u1EDF":"o","\u1EE3":"o","\u1ECD":"o","\u1ED9":"o","\u01EB":"o","\u01ED":"o","\u00F8":"o","\u01FF":"o","\u0254":"o","\uA74B":"o","\uA74D":"o","\u0275":"o","\u01A3":"oi","\u0223":"ou","\uA74F":"oo","\u24DF":"p","\uFF50":"p","\u1E55":"p","\u1E57":"p","\u01A5":"p","\u1D7D":"p","\uA751":"p","\uA753":"p","\uA755":"p","\u24E0":"q","\uFF51":"q","\u024B":"q","\uA757":"q","\uA759":"q","\u24E1":"r","\uFF52":"r","\u0155":"r","\u1E59":"r","\u0159":"r","\u0211":"r","\u0213":"r","\u1E5B":"r","\u1E5D":"r","\u0157":"r","\u1E5F":"r","\u024D":"r","\u027D":"r","\uA75B":"r","\uA7A7":"r","\uA783":"r","\u24E2":"s","\uFF53":"s","\u00DF":"s","\u015B":"s","\u1E65":"s","\u015D":"s","\u1E61":"s","\u0161":"s","\u1E67":"s","\u1E63":"s","\u1E69":"s","\u0219":"s","\u015F":"s","\u023F":"s","\uA7A9":"s","\uA785":"s","\u1E9B":"s","\u24E3":"t","\uFF54":"t","\u1E6B":"t","\u1E97":"t","\u0165":"t","\u1E6D":"t","\u021B":"t","\u0163":"t","\u1E71":"t","\u1E6F":"t","\u0167":"t","\u01AD":"t","\u0288":"t","\u2C66":"t","\uA787":"t","\uA729":"tz","\u24E4":"u","\uFF55":"u","\u00F9":"u","\u00FA":"u","\u00FB":"u","\u0169":"u","\u1E79":"u","\u016B":"u","\u1E7B":"u","\u016D":"u","\u00FC":"u","\u01DC":"u","\u01D8":"u","\u01D6":"u","\u01DA":"u","\u1EE7":"u","\u016F":"u","\u0171":"u","\u01D4":"u","\u0215":"u","\u0217":"u","\u01B0":"u","\u1EEB":"u","\u1EE9":"u","\u1EEF":"u","\u1EED":"u","\u1EF1":"u","\u1EE5":"u","\u1E73":"u","\u0173":"u","\u1E77":"u","\u1E75":"u","\u0289":"u","\u24E5":"v","\uFF56":"v","\u1E7D":"v","\u1E7F":"v","\u028B":"v","\uA75F":"v","\u028C":"v","\uA761":"vy","\u24E6":"w","\uFF57":"w","\u1E81":"w","\u1E83":"w","\u0175":"w","\u1E87":"w","\u1E85":"w","\u1E98":"w","\u1E89":"w","\u2C73":"w","\u24E7":"x","\uFF58":"x","\u1E8B":"x","\u1E8D":"x","\u24E8":"y","\uFF59":"y","\u1EF3":"y","\u00FD":"y","\u0177":"y","\u1EF9":"y","\u0233":"y","\u1E8F":"y","\u00FF":"y","\u1EF7":"y","\u1E99":"y","\u1EF5":"y","\u01B4":"y","\u024F":"y","\u1EFF":"y","\u24E9":"z","\uFF5A":"z","\u017A":"z","\u1E91":"z","\u017C":"z","\u017E":"z","\u1E93":"z","\u1E95":"z","\u01B6":"z","\u0225":"z","\u0240":"z","\u2C6C":"z","\uA763":"z"};

    $document = $(document);

    nextUid=(function() { var counter=1; return function() { return counter++; }; }());


    function reinsertElement(element) {
        var placeholder = $(document.createTextNode(''));

        element.before(placeholder);
        placeholder.before(element);
        placeholder.remove();
    }

    function stripDiacritics(str) {
        // Used 'uni range + named function' from http://jsperf.com/diacritics/18
        function match(a) {
            return DIACRITICS[a] || a;
        }

        return str.replace(/[^\u0000-\u007E]/g, match);
    }

    function indexOf(value, array) {
        var i = 0, l = array.length;
        for (; i < l; i = i + 1) {
            if (equal(value, array[i])) return i;
        }
        return -1;
    }

    function measureScrollbar () {
        var $template = $( MEASURE_SCROLLBAR_TEMPLATE );
        $template.appendTo('body');

        var dim = {
            width: $template.width() - $template[0].clientWidth,
            height: $template.height() - $template[0].clientHeight
        };
        $template.remove();

        return dim;
    }

    /**
     * Compares equality of a and b
     * @param a
     * @param b
     */
    function equal(a, b) {
        if (a === b) return true;
        if (a === undefined || b === undefined) return false;
        if (a === null || b === null) return false;
        // Check whether 'a' or 'b' is a string (primitive or object).
        // The concatenation of an empty string (+'') converts its argument to a string's primitive.
        if (a.constructor === String) return a+'' === b+''; // a+'' - in case 'a' is a String object
        if (b.constructor === String) return b+'' === a+''; // b+'' - in case 'b' is a String object
        return false;
    }

    /**
     * Splits the string into an array of values, trimming each value. An empty array is returned for nulls or empty
     * strings
     * @param string
     * @param separator
     */
    function splitVal(string, separator) {
        var val, i, l;
        if (string === null || string.length < 1) return [];
        val = string.split(separator);
        for (i = 0, l = val.length; i < l; i = i + 1) val[i] = $.trim(val[i]);
        return val;
    }

    function getSideBorderPadding(element) {
        return element.outerWidth(false) - element.width();
    }

    function installKeyUpChangeEvent(element) {
        var key="keyup-change-value";
        element.on("keydown", function () {
            if ($.data(element, key) === undefined) {
                $.data(element, key, element.val());
            }
        });
        element.on("keyup", function () {
            var val= $.data(element, key);
            if (val !== undefined && element.val() !== val) {
                $.removeData(element, key);
                element.trigger("keyup-change");
            }
        });
    }

    $document.on("mousemove", function (e) {
        lastMousePosition.x = e.pageX;
        lastMousePosition.y = e.pageY;
    });

    /**
     * filters mouse events so an event is fired only if the mouse moved.
     *
     * filters out mouse events that occur when mouse is stationary but
     * the elements under the pointer are scrolled.
     */
    function installFilteredMouseMove(element) {
        element.on("mousemove", function (e) {
            var lastpos = lastMousePosition;
            if (lastpos === undefined || lastpos.x !== e.pageX || lastpos.y !== e.pageY) {
                $(e.target).trigger("mousemove-filtered", e);
            }
        });
    }

    /**
     * Debounces a function. Returns a function that calls the original fn function only if no invocations have been made
     * within the last quietMillis milliseconds.
     *
     * @param quietMillis number of milliseconds to wait before invoking fn
     * @param fn function to be debounced
     * @param ctx object to be used as this reference within fn
     * @return debounced version of fn
     */
    function debounce(quietMillis, fn, ctx) {
        ctx = ctx || undefined;
        var timeout;
        return function () {
            var args = arguments;
            window.clearTimeout(timeout);
            timeout = window.setTimeout(function() {
                fn.apply(ctx, args);
            }, quietMillis);
        };
    }

    function installDebouncedScroll(threshold, element) {
        var notify = debounce(threshold, function (e) { element.trigger("scroll-debounced", e);});
        element.on("scroll", function (e) {
            if (indexOf(e.target, element.get()) >= 0) notify(e);
        });
    }

    function focus($el) {
        if ($el[0] === document.activeElement) return;

        /* set the focus in a 0 timeout - that way the focus is set after the processing
            of the current event has finished - which seems like the only reliable way
            to set focus */
        window.setTimeout(function() {
            var el=$el[0], pos=$el.val().length, range;

            $el.focus();

            /* make sure el received focus so we do not error out when trying to manipulate the caret.
                sometimes modals or others listeners may steal it after its set */
            var isVisible = (el.offsetWidth > 0 || el.offsetHeight > 0);
            if (isVisible && el === document.activeElement) {

                /* after the focus is set move the caret to the end, necessary when we val()
                    just before setting focus */
                if(el.setSelectionRange)
                {
                    el.setSelectionRange(pos, pos);
                }
                else if (el.createTextRange) {
                    range = el.createTextRange();
                    range.collapse(false);
                    range.select();
                }
            }
        }, 0);
    }

    function getCursorInfo(el) {
        el = $(el)[0];
        var offset = 0;
        var length = 0;
        if ('selectionStart' in el) {
            offset = el.selectionStart;
            length = el.selectionEnd - offset;
        } else if ('selection' in document) {
            el.focus();
            var sel = document.selection.createRange();
            length = document.selection.createRange().text.length;
            sel.moveStart('character', -el.value.length);
            offset = sel.text.length - length;
        }
        return { offset: offset, length: length };
    }

    function killEvent(event) {
        event.preventDefault();
        event.stopPropagation();
    }
    function killEventImmediately(event) {
        event.preventDefault();
        event.stopImmediatePropagation();
    }

    function measureTextWidth(e) {
        if (!sizer){
            var style = e[0].currentStyle || window.getComputedStyle(e[0], null);
            sizer = $(document.createElement("div")).css({
                position: "absolute",
                left: "-10000px",
                top: "-10000px",
                display: "none",
                fontSize: style.fontSize,
                fontFamily: style.fontFamily,
                fontStyle: style.fontStyle,
                fontWeight: style.fontWeight,
                letterSpacing: style.letterSpacing,
                textTransform: style.textTransform,
                whiteSpace: "nowrap"
            });
            sizer.attr("class","select2-sizer");
            $("body").append(sizer);
        }
        sizer.text(e.val());
        return sizer.width();
    }

    function syncCssClasses(dest, src, adapter) {
        var classes, replacements = [], adapted;

        classes = dest.attr("class");
        if (classes) {
            classes = '' + classes; // for IE which returns object
            $(classes.split(" ")).each2(function() {
                if (this.indexOf("select2-") === 0) {
                    replacements.push(this);
                }
            });
        }
        classes = src.attr("class");
        if (classes) {
            classes = '' + classes; // for IE which returns object
            $(classes.split(" ")).each2(function() {
                if (this.indexOf("select2-") !== 0) {
                    adapted = adapter(this);
                    if (adapted) {
                        replacements.push(adapted);
                    }
                }
            });
        }
        dest.attr("class", replacements.join(" "));
    }


    function markMatch(text, term, markup, escapeMarkup) {
        var match=stripDiacritics(text.toUpperCase()).indexOf(stripDiacritics(term.toUpperCase())),
            tl=term.length;

        if (match<0) {
            markup.push(escapeMarkup(text));
            return;
        }

        markup.push(escapeMarkup(text.substring(0, match)));
        markup.push("<span class='select2-match'>");
        markup.push(escapeMarkup(text.substring(match, match + tl)));
        markup.push("</span>");
        markup.push(escapeMarkup(text.substring(match + tl, text.length)));
    }

    function defaultEscapeMarkup(markup) {
        var replace_map = {
            '\\': '&#92;',
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;',
            "/": '&#47;'
        };

        return String(markup).replace(/[&<>"'\/\\]/g, function (match) {
            return replace_map[match];
        });
    }

    /**
     * Produces an ajax-based query function
     *
     * @param options object containing configuration parameters
     * @param options.params parameter map for the transport ajax call, can contain such options as cache, jsonpCallback, etc. see $.ajax
     * @param options.transport function that will be used to execute the ajax request. must be compatible with parameters supported by $.ajax
     * @param options.url url for the data
     * @param options.data a function(searchTerm, pageNumber, context) that should return an object containing query string parameters for the above url.
     * @param options.dataType request data type: ajax, jsonp, other datatypes supported by jQuery's $.ajax function or the transport function if specified
     * @param options.quietMillis (optional) milliseconds to wait before making the ajaxRequest, helps debounce the ajax function if invoked too often
     * @param options.results a function(remoteData, pageNumber) that converts data returned form the remote request to the format expected by Select2.
     *      The expected format is an object containing the following keys:
     *      results array of objects that will be used as choices
     *      more (optional) boolean indicating whether there are more results available
     *      Example: {results:[{id:1, text:'Red'},{id:2, text:'Blue'}], more:true}
     */
    function ajax(options) {
        var timeout, // current scheduled but not yet executed request
            handler = null,
            quietMillis = options.quietMillis || 100,
            ajaxUrl = options.url,
            self = this;

        return function (query) {
            window.clearTimeout(timeout);
            timeout = window.setTimeout(function () {
                var data = options.data, // ajax data function
                    url = ajaxUrl, // ajax url string or function
                    transport = options.transport || $.fn.select2.ajaxDefaults.transport,
                    // deprecated - to be removed in 4.0  - use params instead
                    deprecated = {
                        type: options.type || 'GET', // set type of request (GET or POST)
                        cache: options.cache || false,
                        jsonpCallback: options.jsonpCallback||undefined,
                        dataType: options.dataType||"json"
                    },
                    params = $.extend({}, $.fn.select2.ajaxDefaults.params, deprecated);

                data = data ? data.call(self, query.term, query.page, query.context) : null;
                url = (typeof url === 'function') ? url.call(self, query.term, query.page, query.context) : url;

                if (handler && typeof handler.abort === "function") { handler.abort(); }

                if (options.params) {
                    if ($.isFunction(options.params)) {
                        $.extend(params, options.params.call(self));
                    } else {
                        $.extend(params, options.params);
                    }
                }

                $.extend(params, {
                    url: url,
                    dataType: options.dataType,
                    data: data,
                    success: function (data) {
                        // TODO - replace query.page with query so users have access to term, page, etc.
                        var results = options.results(data, query.page);
                        query.callback(results);
                    }
                });
                handler = transport.call(self, params);
            }, quietMillis);
        };
    }

    /**
     * Produces a query function that works with a local array
     *
     * @param options object containing configuration parameters. The options parameter can either be an array or an
     * object.
     *
     * If the array form is used it is assumed that it contains objects with 'id' and 'text' keys.
     *
     * If the object form is used it is assumed that it contains 'data' and 'text' keys. The 'data' key should contain
     * an array of objects that will be used as choices. These objects must contain at least an 'id' key. The 'text'
     * key can either be a String in which case it is expected that each element in the 'data' array has a key with the
     * value of 'text' which will be used to match choices. Alternatively, text can be a function(item) that can extract
     * the text.
     */
    function local(options) {
        var data = options, // data elements
            dataText,
            tmp,
            text = function (item) { return ""+item.text; }; // function used to retrieve the text portion of a data item that is matched against the search

         if ($.isArray(data)) {
            tmp = data;
            data = { results: tmp };
        }

         if ($.isFunction(data) === false) {
            tmp = data;
            data = function() { return tmp; };
        }

        var dataItem = data();
        if (dataItem.text) {
            text = dataItem.text;
            // if text is not a function we assume it to be a key name
            if (!$.isFunction(text)) {
                dataText = dataItem.text; // we need to store this in a separate variable because in the next step data gets reset and data.text is no longer available
                text = function (item) { return item[dataText]; };
            }
        }

        return function (query) {
            var t = query.term, filtered = { results: [] }, process;
            if (t === "") {
                query.callback(data());
                return;
            }

            process = function(datum, collection) {
                var group, attr;
                datum = datum[0];
                if (datum.children) {
                    group = {};
                    for (attr in datum) {
                        if (datum.hasOwnProperty(attr)) group[attr]=datum[attr];
                    }
                    group.children=[];
                    $(datum.children).each2(function(i, childDatum) { process(childDatum, group.children); });
                    if (group.children.length || query.matcher(t, text(group), datum)) {
                        collection.push(group);
                    }
                } else {
                    if (query.matcher(t, text(datum), datum)) {
                        collection.push(datum);
                    }
                }
            };

            $(data().results).each2(function(i, datum) { process(datum, filtered.results); });
            query.callback(filtered);
        };
    }

    // TODO javadoc
    function tags(data) {
        var isFunc = $.isFunction(data);
        return function (query) {
            var t = query.term, filtered = {results: []};
            var result = isFunc ? data(query) : data;
            if ($.isArray(result)) {
                $(result).each(function () {
                    var isObject = this.text !== undefined,
                        text = isObject ? this.text : this;
                    if (t === "" || query.matcher(t, text)) {
                        filtered.results.push(isObject ? this : {id: this, text: this});
                    }
                });
                query.callback(filtered);
            }
        };
    }

    /**
     * Checks if the formatter function should be used.
     *
     * Throws an error if it is not a function. Returns true if it should be used,
     * false if no formatting should be performed.
     *
     * @param formatter
     */
    function checkFormatter(formatter, formatterName) {
        if ($.isFunction(formatter)) return true;
        if (!formatter) return false;
        if (typeof(formatter) === 'string') return true;
        throw new Error(formatterName +" must be a string, function, or falsy value");
    }

    function evaluate(val) {
        if ($.isFunction(val)) {
            var args = Array.prototype.slice.call(arguments, 1);
            return val.apply(null, args);
        }
        return val;
    }

    function countResults(results) {
        var count = 0;
        $.each(results, function(i, item) {
            if (item.children) {
                count += countResults(item.children);
            } else {
                count++;
            }
        });
        return count;
    }

    /**
     * Default tokenizer. This function uses breaks the input on substring match of any string from the
     * opts.tokenSeparators array and uses opts.createSearchChoice to create the choice object. Both of those
     * two options have to be defined in order for the tokenizer to work.
     *
     * @param input text user has typed so far or pasted into the search field
     * @param selection currently selected choices
     * @param selectCallback function(choice) callback tho add the choice to selection
     * @param opts select2's opts
     * @return undefined/null to leave the current input unchanged, or a string to change the input to the returned value
     */
    function defaultTokenizer(input, selection, selectCallback, opts) {
        var original = input, // store the original so we can compare and know if we need to tell the search to update its text
            dupe = false, // check for whether a token we extracted represents a duplicate selected choice
            token, // token
            index, // position at which the separator was found
            i, l, // looping variables
            separator; // the matched separator

        if (!opts.createSearchChoice || !opts.tokenSeparators || opts.tokenSeparators.length < 1) return undefined;

        while (true) {
            index = -1;

            for (i = 0, l = opts.tokenSeparators.length; i < l; i++) {
                separator = opts.tokenSeparators[i];
                index = input.indexOf(separator);
                if (index >= 0) break;
            }

            if (index < 0) break; // did not find any token separator in the input string, bail

            token = input.substring(0, index);
            input = input.substring(index + separator.length);

            if (token.length > 0) {
                token = opts.createSearchChoice.call(this, token, selection);
                if (token !== undefined && token !== null && opts.id(token) !== undefined && opts.id(token) !== null) {
                    dupe = false;
                    for (i = 0, l = selection.length; i < l; i++) {
                        if (equal(opts.id(token), opts.id(selection[i]))) {
                            dupe = true; break;
                        }
                    }

                    if (!dupe) selectCallback(token);
                }
            }
        }

        if (original!==input) return input;
    }

    function cleanupJQueryElements() {
        var self = this;

        Array.prototype.forEach.call(arguments, function (element) {
            self[element].remove();
            self[element] = null;
        });
    }

    /**
     * Creates a new class
     *
     * @param superClass
     * @param methods
     */
    function clazz(SuperClass, methods) {
        var constructor = function () {};
        constructor.prototype = new SuperClass;
        constructor.prototype.constructor = constructor;
        constructor.prototype.parent = SuperClass.prototype;
        constructor.prototype = $.extend(constructor.prototype, methods);
        return constructor;
    }

    AbstractSelect2 = clazz(Object, {

        // abstract
        bind: function (func) {
            var self = this;
            return function () {
                func.apply(self, arguments);
            };
        },

        // abstract
        init: function (opts) {
            var results, search, resultsSelector = ".select2-results";

            // prepare options
            this.opts = opts = this.prepareOpts(opts);

            this.id=opts.id;

            // destroy if called on an existing component
            if (opts.element.data("select2") !== undefined &&
                opts.element.data("select2") !== null) {
                opts.element.data("select2").destroy();
            }

            this.container = this.createContainer();

            this.liveRegion = $("<span>", {
                    role: "status",
                    "aria-live": "polite"
                })
                .addClass("select2-hidden-accessible")
                .appendTo(document.body);

            this.containerId="s2id_"+(opts.element.attr("id") || "autogen"+nextUid());
            this.containerEventName= this.containerId
                .replace(/([.])/g, '_')
                .replace(/([;&,\-\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
            this.container.attr("id", this.containerId);

            this.container.attr("title", opts.element.attr("title"));

            this.body = $("body");

            syncCssClasses(this.container, this.opts.element, this.opts.adaptContainerCssClass);

            this.container.attr("style", opts.element.attr("style"));
            this.container.css(evaluate(opts.containerCss));
            this.container.addClass(evaluate(opts.containerCssClass));

            this.elementTabIndex = this.opts.element.attr("tabindex");

            // swap container for the element
            this.opts.element
                .data("select2", this)
                .attr("tabindex", "-1")
                .before(this.container)
                .on("click.select2", killEvent); // do not leak click events

            this.container.data("select2", this);

            this.dropdown = this.container.find(".select2-drop");

            syncCssClasses(this.dropdown, this.opts.element, this.opts.adaptDropdownCssClass);

            this.dropdown.addClass(evaluate(opts.dropdownCssClass));
            this.dropdown.data("select2", this);
            this.dropdown.on("click", killEvent);

            this.results = results = this.container.find(resultsSelector);
            this.search = search = this.container.find("input.select2-input");

            this.queryCount = 0;
            this.resultsPage = 0;
            this.context = null;

            // initialize the container
            this.initContainer();

            this.container.on("click", killEvent);

            installFilteredMouseMove(this.results);

            this.dropdown.on("mousemove-filtered", resultsSelector, this.bind(this.highlightUnderEvent));
            this.dropdown.on("touchstart touchmove touchend", resultsSelector, this.bind(function (event) {
                this._touchEvent = true;
                this.highlightUnderEvent(event);
            }));
            this.dropdown.on("touchmove", resultsSelector, this.bind(this.touchMoved));
            this.dropdown.on("touchstart touchend", resultsSelector, this.bind(this.clearTouchMoved));

            // Waiting for a click event on touch devices to select option and hide dropdown
            // otherwise click will be triggered on an underlying element
            this.dropdown.on('click', this.bind(function (event) {
                if (this._touchEvent) {
                    this._touchEvent = false;
                    this.selectHighlighted();
                }
            }));

            installDebouncedScroll(80, this.results);
            this.dropdown.on("scroll-debounced", resultsSelector, this.bind(this.loadMoreIfNeeded));

            // do not propagate change event from the search field out of the component
            $(this.container).on("change", ".select2-input", function(e) {e.stopPropagation();});
            $(this.dropdown).on("change", ".select2-input", function(e) {e.stopPropagation();});

            // if jquery.mousewheel plugin is installed we can prevent out-of-bounds scrolling of results via mousewheel
            if ($.fn.mousewheel) {
                results.mousewheel(function (e, delta, deltaX, deltaY) {
                    var top = results.scrollTop();
                    if (deltaY > 0 && top - deltaY <= 0) {
                        results.scrollTop(0);
                        killEvent(e);
                    } else if (deltaY < 0 && results.get(0).scrollHeight - results.scrollTop() + deltaY <= results.height()) {
                        results.scrollTop(results.get(0).scrollHeight - results.height());
                        killEvent(e);
                    }
                });
            }

            installKeyUpChangeEvent(search);
            search.on("keyup-change input paste", this.bind(this.updateResults));
            search.on("focus", function () { search.addClass("select2-focused"); });
            search.on("blur", function () { search.removeClass("select2-focused");});

            this.dropdown.on("mouseup", resultsSelector, this.bind(function (e) {
                if ($(e.target).closest(".select2-result-selectable").length > 0) {
                    this.highlightUnderEvent(e);
                    this.selectHighlighted(e);
                }
            }));

            // trap all mouse events from leaving the dropdown. sometimes there may be a modal that is listening
            // for mouse events outside of itself so it can close itself. since the dropdown is now outside the select2's
            // dom it will trigger the popup close, which is not what we want
            // focusin can cause focus wars between modals and select2 since the dropdown is outside the modal.
            this.dropdown.on("click mouseup mousedown touchstart touchend focusin", function (e) { e.stopPropagation(); });

            this.nextSearchTerm = undefined;

            if ($.isFunction(this.opts.initSelection)) {
                // initialize selection based on the current value of the source element
                this.initSelection();

                // if the user has provided a function that can set selection based on the value of the source element
                // we monitor the change event on the element and trigger it, allowing for two way synchronization
                this.monitorSource();
            }

            if (opts.maximumInputLength !== null) {
                this.search.attr("maxlength", opts.maximumInputLength);
            }

            var disabled = opts.element.prop("disabled");
            if (disabled === undefined) disabled = false;
            this.enable(!disabled);

            var readonly = opts.element.prop("readonly");
            if (readonly === undefined) readonly = false;
            this.readonly(readonly);

            // Calculate size of scrollbar
            scrollBarDimensions = scrollBarDimensions || measureScrollbar();

            this.autofocus = opts.element.prop("autofocus");
            opts.element.prop("autofocus", false);
            if (this.autofocus) this.focus();

            this.search.attr("placeholder", opts.searchInputPlaceholder);
        },

        // abstract
        destroy: function () {
            var element=this.opts.element, select2 = element.data("select2");

            this.close();

            if (this.propertyObserver) {
                this.propertyObserver.disconnect();
                this.propertyObserver = null;
            }

            if (select2 !== undefined) {
                select2.container.remove();
                select2.liveRegion.remove();
                select2.dropdown.remove();
                element
                    .removeClass("select2-offscreen")
                    .removeData("select2")
                    .off(".select2")
                    .prop("autofocus", this.autofocus || false);
                if (this.elementTabIndex) {
                    element.attr({tabindex: this.elementTabIndex});
                } else {
                    element.removeAttr("tabindex");
                }
                element.show();
            }

            cleanupJQueryElements.call(this,
                "container",
                "liveRegion",
                "dropdown",
                "results",
                "search"
            );
        },

        // abstract
        optionToData: function(element) {
            if (element.is("option")) {
                return {
                    id:element.prop("value"),
                    text:element.text(),
                    element: element.get(),
                    css: element.attr("class"),
                    disabled: element.prop("disabled"),
                    locked: equal(element.attr("locked"), "locked") || equal(element.data("locked"), true)
                };
            } else if (element.is("optgroup")) {
                return {
                    text:element.attr("label"),
                    children:[],
                    element: element.get(),
                    css: element.attr("class")
                };
            }
        },

        // abstract
        prepareOpts: function (opts) {
            var element, select, idKey, ajaxUrl, self = this;

            element = opts.element;

            if (element.get(0).tagName.toLowerCase() === "select") {
                this.select = select = opts.element;
            }

            if (select) {
                // these options are not allowed when attached to a select because they are picked up off the element itself
                $.each(["id", "multiple", "ajax", "query", "createSearchChoice", "initSelection", "data", "tags"], function () {
                    if (this in opts) {
                        throw new Error("Option '" + this + "' is not allowed for Select2 when attached to a <select> element.");
                    }
                });
            }

            opts = $.extend({}, {
                populateResults: function(container, results, query) {
                    var populate, id=this.opts.id, liveRegion=this.liveRegion;

                    populate=function(results, container, depth) {

                        var i, l, result, selectable, disabled, compound, node, label, innerContainer, formatted;

                        results = opts.sortResults(results, container, query);

                        for (i = 0, l = results.length; i < l; i = i + 1) {

                            result=results[i];

                            disabled = (result.disabled === true);
                            selectable = (!disabled) && (id(result) !== undefined);

                            compound=result.children && result.children.length > 0;

                            node=$("<li></li>");
                            node.addClass("select2-results-dept-"+depth);
                            node.addClass("select2-result");
                            node.addClass(selectable ? "select2-result-selectable" : "select2-result-unselectable");
                            if (disabled) { node.addClass("select2-disabled"); }
                            if (compound) { node.addClass("select2-result-with-children"); }
                            node.addClass(self.opts.formatResultCssClass(result));
                            node.attr("role", "presentation");

                            label=$(document.createElement("div"));
                            label.addClass("select2-result-label");
                            label.attr("id", "select2-result-label-" + nextUid());
                            label.attr("role", "option");

                            formatted=opts.formatResult(result, label, query, self.opts.escapeMarkup);
                            if (formatted!==undefined) {
                                label.html(formatted);
                                node.append(label);
                            }


                            if (compound) {

                                innerContainer=$("<ul></ul>");
                                innerContainer.addClass("select2-result-sub");
                                populate(result.children, innerContainer, depth+1);
                                node.append(innerContainer);
                            }

                            node.data("select2-data", result);
                            container.append(node);
                        }

                        liveRegion.text(opts.formatMatches(results.length));
                    };

                    populate(results, container, 0);
                }
            }, $.fn.select2.defaults, opts);

            if (typeof(opts.id) !== "function") {
                idKey = opts.id;
                opts.id = function (e) { return e[idKey]; };
            }

            if ($.isArray(opts.element.data("select2Tags"))) {
                if ("tags" in opts) {
                    throw "tags specified as both an attribute 'data-select2-tags' and in options of Select2 " + opts.element.attr("id");
                }
                opts.tags=opts.element.data("select2Tags");
            }

            if (select) {
                opts.query = this.bind(function (query) {
                    var data = { results: [], more: false },
                        term = query.term,
                        children, placeholderOption, process;

                    process=function(element, collection) {
                        var group;
                        if (element.is("option")) {
                            if (query.matcher(term, element.text(), element)) {
                                collection.push(self.optionToData(element));
                            }
                        } else if (element.is("optgroup")) {
                            group=self.optionToData(element);
                            element.children().each2(function(i, elm) { process(elm, group.children); });
                            if (group.children.length>0) {
                                collection.push(group);
                            }
                        }
                    };

                    children=element.children();

                    // ignore the placeholder option if there is one
                    if (this.getPlaceholder() !== undefined && children.length > 0) {
                        placeholderOption = this.getPlaceholderOption();
                        if (placeholderOption) {
                            children=children.not(placeholderOption);
                        }
                    }

                    children.each2(function(i, elm) { process(elm, data.results); });

                    query.callback(data);
                });
                // this is needed because inside val() we construct choices from options and there id is hardcoded
                opts.id=function(e) { return e.id; };
            } else {
                if (!("query" in opts)) {

                    if ("ajax" in opts) {
                        ajaxUrl = opts.element.data("ajax-url");
                        if (ajaxUrl && ajaxUrl.length > 0) {
                            opts.ajax.url = ajaxUrl;
                        }
                        opts.query = ajax.call(opts.element, opts.ajax);
                    } else if ("data" in opts) {
                        opts.query = local(opts.data);
                    } else if ("tags" in opts) {
                        opts.query = tags(opts.tags);
                        if (opts.createSearchChoice === undefined) {
                            opts.createSearchChoice = function (term) { return {id: $.trim(term), text: $.trim(term)}; };
                        }
                        if (opts.initSelection === undefined) {
                            opts.initSelection = function (element, callback) {
                                var data = [];
                                $(splitVal(element.val(), opts.separator)).each(function () {
                                    var obj = { id: this, text: this },
                                        tags = opts.tags;
                                    if ($.isFunction(tags)) tags=tags();
                                    $(tags).each(function() { if (equal(this.id, obj.id)) { obj = this; return false; } });
                                    data.push(obj);
                                });

                                callback(data);
                            };
                        }
                    }
                }
            }
            if (typeof(opts.query) !== "function") {
                throw "query function not defined for Select2 " + opts.element.attr("id");
            }

            if (opts.createSearchChoicePosition === 'top') {
                opts.createSearchChoicePosition = function(list, item) { list.unshift(item); };
            }
            else if (opts.createSearchChoicePosition === 'bottom') {
                opts.createSearchChoicePosition = function(list, item) { list.push(item); };
            }
            else if (typeof(opts.createSearchChoicePosition) !== "function")  {
                throw "invalid createSearchChoicePosition option must be 'top', 'bottom' or a custom function";
            }

            return opts;
        },

        /**
         * Monitor the original element for changes and update select2 accordingly
         */
        // abstract
        monitorSource: function () {
            var el = this.opts.element, sync, observer;

            el.on("change.select2", this.bind(function (e) {
                if (this.opts.element.data("select2-change-triggered") !== true) {
                    this.initSelection();
                }
            }));

            sync = this.bind(function () {

                // sync enabled state
                var disabled = el.prop("disabled");
                if (disabled === undefined) disabled = false;
                this.enable(!disabled);

                var readonly = el.prop("readonly");
                if (readonly === undefined) readonly = false;
                this.readonly(readonly);

                syncCssClasses(this.container, this.opts.element, this.opts.adaptContainerCssClass);
                this.container.addClass(evaluate(this.opts.containerCssClass));

                syncCssClasses(this.dropdown, this.opts.element, this.opts.adaptDropdownCssClass);
                this.dropdown.addClass(evaluate(this.opts.dropdownCssClass));

            });

            // IE8-10 (IE9/10 won't fire propertyChange via attachEventListener)
            if (el.length && el[0].attachEvent) {
                el.each(function() {
                    this.attachEvent("onpropertychange", sync);
                });
            }
            
            // safari, chrome, firefox, IE11
            observer = window.MutationObserver || window.WebKitMutationObserver|| window.MozMutationObserver;
            if (observer !== undefined) {
                if (this.propertyObserver) { delete this.propertyObserver; this.propertyObserver = null; }
                this.propertyObserver = new observer(function (mutations) {
                    mutations.forEach(sync);
                });
                this.propertyObserver.observe(el.get(0), { attributes:true, subtree:false });
            }
        },

        // abstract
        triggerSelect: function(data) {
            var evt = $.Event("select2-selecting", { val: this.id(data), object: data });
            this.opts.element.trigger(evt);
            return !evt.isDefaultPrevented();
        },

        /**
         * Triggers the change event on the source element
         */
        // abstract
        triggerChange: function (details) {

            details = details || {};
            details= $.extend({}, details, { type: "change", val: this.val() });
            // prevents recursive triggering
            this.opts.element.data("select2-change-triggered", true);
            this.opts.element.trigger(details);
            this.opts.element.data("select2-change-triggered", false);

            // some validation frameworks ignore the change event and listen instead to keyup, click for selects
            // so here we trigger the click event manually
            this.opts.element.click();

            // ValidationEngine ignores the change event and listens instead to blur
            // so here we trigger the blur event manually if so desired
            if (this.opts.blurOnChange)
                this.opts.element.blur();
        },

        //abstract
        isInterfaceEnabled: function()
        {
            return this.enabledInterface === true;
        },

        // abstract
        enableInterface: function() {
            var enabled = this._enabled && !this._readonly,
                disabled = !enabled;

            if (enabled === this.enabledInterface) return false;

            this.container.toggleClass("select2-container-disabled", disabled);
            this.close();
            this.enabledInterface = enabled;

            return true;
        },

        // abstract
        enable: function(enabled) {
            if (enabled === undefined) enabled = true;
            if (this._enabled === enabled) return;
            this._enabled = enabled;

            this.opts.element.prop("disabled", !enabled);
            this.enableInterface();
        },

        // abstract
        disable: function() {
            this.enable(false);
        },

        // abstract
        readonly: function(enabled) {
            if (enabled === undefined) enabled = false;
            if (this._readonly === enabled) return;
            this._readonly = enabled;

            this.opts.element.prop("readonly", enabled);
            this.enableInterface();
        },

        // abstract
        opened: function () {
            return this.container.hasClass("select2-dropdown-open");
        },

        // abstract
        positionDropdown: function() {
            var $dropdown = this.dropdown,
                offset = this.container.offset(),
                height = this.container.outerHeight(false),
                width = this.container.outerWidth(false),
                dropHeight = $dropdown.outerHeight(false),
                $window = $(window),
                windowWidth = $window.width(),
                windowHeight = $window.height(),
                viewPortRight = $window.scrollLeft() + windowWidth,
                viewportBottom = $window.scrollTop() + windowHeight,
                dropTop = offset.top + height,
                dropLeft = offset.left,
                enoughRoomBelow = dropTop + dropHeight <= viewportBottom,
                enoughRoomAbove = (offset.top - dropHeight) >= $window.scrollTop(),
                dropWidth = $dropdown.outerWidth(false),
                enoughRoomOnRight = dropLeft + dropWidth <= viewPortRight,
                aboveNow = $dropdown.hasClass("select2-drop-above"),
                bodyOffset,
                above,
                changeDirection,
                css,
                resultsListNode;

            // always prefer the current above/below alignment, unless there is not enough room
            if (aboveNow) {
                above = true;
                if (!enoughRoomAbove && enoughRoomBelow) {
                    changeDirection = true;
                    above = false;
                }
            } else {
                above = false;
                if (!enoughRoomBelow && enoughRoomAbove) {
                    changeDirection = true;
                    above = true;
                }
            }

            //if we are changing direction we need to get positions when dropdown is hidden;
            if (changeDirection) {
                $dropdown.hide();
                offset = this.container.offset();
                height = this.container.outerHeight(false);
                width = this.container.outerWidth(false);
                dropHeight = $dropdown.outerHeight(false);
                viewPortRight = $window.scrollLeft() + windowWidth;
                viewportBottom = $window.scrollTop() + windowHeight;
                dropTop = offset.top + height;
                dropLeft = offset.left;
                dropWidth = $dropdown.outerWidth(false);
                enoughRoomOnRight = dropLeft + dropWidth <= viewPortRight;
                $dropdown.show();

                // fix so the cursor does not move to the left within the search-textbox in IE
                this.focusSearch();
            }

            if (this.opts.dropdownAutoWidth) {
                resultsListNode = $('.select2-results', $dropdown)[0];
                $dropdown.addClass('select2-drop-auto-width');
                $dropdown.css('width', '');
                // Add scrollbar width to dropdown if vertical scrollbar is present
                dropWidth = $dropdown.outerWidth(false) + (resultsListNode.scrollHeight === resultsListNode.clientHeight ? 0 : scrollBarDimensions.width);
                dropWidth > width ? width = dropWidth : dropWidth = width;
                dropHeight = $dropdown.outerHeight(false);
                enoughRoomOnRight = dropLeft + dropWidth <= viewPortRight;
            }
            else {
                this.container.removeClass('select2-drop-auto-width');
            }

            //console.log("below/ droptop:", dropTop, "dropHeight", dropHeight, "sum", (dropTop+dropHeight)+" viewport bottom", viewportBottom, "enough?", enoughRoomBelow);
            //console.log("above/ offset.top", offset.top, "dropHeight", dropHeight, "top", (offset.top-dropHeight), "scrollTop", this.body.scrollTop(), "enough?", enoughRoomAbove);

            // fix positioning when body has an offset and is not position: static
            if (this.body.css('position') !== 'static') {
                bodyOffset = this.body.offset();
                dropTop -= bodyOffset.top;
                dropLeft -= bodyOffset.left;
            }

            if (!enoughRoomOnRight) {
                dropLeft = offset.left + this.container.outerWidth(false) - dropWidth;
            }

            css =  {
                left: dropLeft,
                width: width
            };

            if (above) {
                css.top = offset.top - dropHeight;
                css.bottom = 'auto';
                this.container.addClass("select2-drop-above");
                $dropdown.addClass("select2-drop-above");
            }
            else {
                css.top = dropTop;
                css.bottom = 'auto';
                this.container.removeClass("select2-drop-above");
                $dropdown.removeClass("select2-drop-above");
            }
            css = $.extend(css, evaluate(this.opts.dropdownCss));

            $dropdown.css(css);
        },

        // abstract
        shouldOpen: function() {
            var event;

            if (this.opened()) return false;

            if (this._enabled === false || this._readonly === true) return false;

            event = $.Event("select2-opening");
            this.opts.element.trigger(event);
            return !event.isDefaultPrevented();
        },

        // abstract
        clearDropdownAlignmentPreference: function() {
            // clear the classes used to figure out the preference of where the dropdown should be opened
            this.container.removeClass("select2-drop-above");
            this.dropdown.removeClass("select2-drop-above");
        },

        /**
         * Opens the dropdown
         *
         * @return {Boolean} whether or not dropdown was opened. This method will return false if, for example,
         * the dropdown is already open, or if the 'open' event listener on the element called preventDefault().
         */
        // abstract
        open: function () {

            if (!this.shouldOpen()) return false;

            this.opening();

            return true;
        },

        /**
         * Performs the opening of the dropdown
         */
        // abstract
        opening: function() {
            var cid = this.containerEventName,
                scroll = "scroll." + cid,
                resize = "resize."+cid,
                orient = "orientationchange."+cid,
                mask;

            this.container.addClass("select2-dropdown-open").addClass("select2-container-active");

            this.clearDropdownAlignmentPreference();

            if(this.dropdown[0] !== this.body.children().last()[0]) {
                this.dropdown.detach().appendTo(this.body);
            }

            // create the dropdown mask if doesn't already exist
            mask = $("#select2-drop-mask");
            if (mask.length == 0) {
                mask = $(document.createElement("div"));
                mask.attr("id","select2-drop-mask").attr("class","select2-drop-mask");
                mask.hide();
                mask.appendTo(this.body);
                mask.on("mousedown touchstart click", function (e) {
                    // Prevent IE from generating a click event on the body
                    reinsertElement(mask);

                    var dropdown = $("#select2-drop"), self;
                    if (dropdown.length > 0) {
                        self=dropdown.data("select2");
                        if (self.opts.selectOnBlur) {
                            self.selectHighlighted({noFocus: true});
                        }
                        self.close();
                        e.preventDefault();
                        e.stopPropagation();
                    }
                });
            }

            // ensure the mask is always right before the dropdown
            if (this.dropdown.prev()[0] !== mask[0]) {
                this.dropdown.before(mask);
            }

            // move the global id to the correct dropdown
            $("#select2-drop").removeAttr("id");
            this.dropdown.attr("id", "select2-drop");

            // show the elements
            mask.show();

            this.positionDropdown();
            this.dropdown.show();
            this.positionDropdown();

            this.dropdown.addClass("select2-drop-active");

            // attach listeners to events that can change the position of the container and thus require
            // the position of the dropdown to be updated as well so it does not come unglued from the container
            var that = this;
            this.container.parents().add(window).each(function () {
                $(this).on(resize+" "+scroll+" "+orient, function (e) {
                    if (that.opened()) that.positionDropdown();
                });
            });


        },

        // abstract
        close: function () {
            if (!this.opened()) return;

            var cid = this.containerEventName,
                scroll = "scroll." + cid,
                resize = "resize."+cid,
                orient = "orientationchange."+cid;

            // unbind event listeners
            this.container.parents().add(window).each(function () { $(this).off(scroll).off(resize).off(orient); });

            this.clearDropdownAlignmentPreference();

            $("#select2-drop-mask").hide();
            this.dropdown.removeAttr("id"); // only the active dropdown has the select2-drop id
            this.dropdown.hide();
            this.container.removeClass("select2-dropdown-open").removeClass("select2-container-active");
            this.results.empty();


            this.clearSearch();
            this.search.removeClass("select2-active");
            this.opts.element.trigger($.Event("select2-close"));
        },

        /**
         * Opens control, sets input value, and updates results.
         */
        // abstract
        externalSearch: function (term) {
            this.open();
            this.search.val(term);
            this.updateResults(false);
        },

        // abstract
        clearSearch: function () {

        },

        //abstract
        getMaximumSelectionSize: function() {
            return evaluate(this.opts.maximumSelectionSize);
        },

        // abstract
        ensureHighlightVisible: function () {
            var results = this.results, children, index, child, hb, rb, y, more;

            index = this.highlight();

            if (index < 0) return;

            if (index == 0) {

                // if the first element is highlighted scroll all the way to the top,
                // that way any unselectable headers above it will also be scrolled
                // into view

                results.scrollTop(0);
                return;
            }

            children = this.findHighlightableChoices().find('.select2-result-label');

            child = $(children[index]);

            hb = child.offset().top + child.outerHeight(true);

            // if this is the last child lets also make sure select2-more-results is visible
            if (index === children.length - 1) {
                more = results.find("li.select2-more-results");
                if (more.length > 0) {
                    hb = more.offset().top + more.outerHeight(true);
                }
            }

            rb = results.offset().top + results.outerHeight(true);
            if (hb > rb) {
                results.scrollTop(results.scrollTop() + (hb - rb));
            }
            y = child.offset().top - results.offset().top;

            // make sure the top of the element is visible
            if (y < 0 && child.css('display') != 'none' ) {
                results.scrollTop(results.scrollTop() + y); // y is negative
            }
        },

        // abstract
        findHighlightableChoices: function() {
            return this.results.find(".select2-result-selectable:not(.select2-disabled):not(.select2-selected)");
        },

        // abstract
        moveHighlight: function (delta) {
            var choices = this.findHighlightableChoices(),
                index = this.highlight();

            while (index > -1 && index < choices.length) {
                index += delta;
                var choice = $(choices[index]);
                if (choice.hasClass("select2-result-selectable") && !choice.hasClass("select2-disabled") && !choice.hasClass("select2-selected")) {
                    this.highlight(index);
                    break;
                }
            }
        },

        // abstract
        highlight: function (index) {
            var choices = this.findHighlightableChoices(),
                choice,
                data;

            if (arguments.length === 0) {
                return indexOf(choices.filter(".select2-highlighted")[0], choices.get());
            }

            if (index >= choices.length) index = choices.length - 1;
            if (index < 0) index = 0;

            this.removeHighlight();

            choice = $(choices[index]);
            choice.addClass("select2-highlighted");

            // ensure assistive technology can determine the active choice
            this.search.attr("aria-activedescendant", choice.find(".select2-result-label").attr("id"));

            this.ensureHighlightVisible();

            this.liveRegion.text(choice.text());

            data = choice.data("select2-data");
            if (data) {
                this.opts.element.trigger({ type: "select2-highlight", val: this.id(data), choice: data });
            }
        },

        removeHighlight: function() {
            this.results.find(".select2-highlighted").removeClass("select2-highlighted");
        },

        touchMoved: function() {
            this._touchMoved = true;
        },

        clearTouchMoved: function() {
          this._touchMoved = false;
        },

        // abstract
        countSelectableResults: function() {
            return this.findHighlightableChoices().length;
        },

        // abstract
        highlightUnderEvent: function (event) {
            var el = $(event.target).closest(".select2-result-selectable");
            if (el.length > 0 && !el.is(".select2-highlighted")) {
                var choices = this.findHighlightableChoices();
                this.highlight(choices.index(el));
            } else if (el.length == 0) {
                // if we are over an unselectable item remove all highlights
                this.removeHighlight();
            }
        },

        // abstract
        loadMoreIfNeeded: function () {
            var results = this.results,
                more = results.find("li.select2-more-results"),
                below, // pixels the element is below the scroll fold, below==0 is when the element is starting to be visible
                page = this.resultsPage + 1,
                self=this,
                term=this.search.val(),
                context=this.context;

            if (more.length === 0) return;
            below = more.offset().top - results.offset().top - results.height();

            if (below <= this.opts.loadMorePadding) {
                more.addClass("select2-active");
                this.opts.query({
                        element: this.opts.element,
                        term: term,
                        page: page,
                        context: context,
                        matcher: this.opts.matcher,
                        callback: this.bind(function (data) {

                    // ignore a response if the select2 has been closed before it was received
                    if (!self.opened()) return;


                    self.opts.populateResults.call(this, results, data.results, {term: term, page: page, context:context});
                    self.postprocessResults(data, false, false);

                    if (data.more===true) {
                        more.detach().appendTo(results).text(evaluate(self.opts.formatLoadMore, page+1));
                        window.setTimeout(function() { self.loadMoreIfNeeded(); }, 10);
                    } else {
                        more.remove();
                    }
                    self.positionDropdown();
                    self.resultsPage = page;
                    self.context = data.context;
                    this.opts.element.trigger({ type: "select2-loaded", items: data });
                })});
            }
        },

        /**
         * Default tokenizer function which does nothing
         */
        tokenize: function() {

        },

        /**
         * @param initial whether or not this is the call to this method right after the dropdown has been opened
         */
        // abstract
        updateResults: function (initial) {
            var search = this.search,
                results = this.results,
                opts = this.opts,
                data,
                self = this,
                input,
                term = search.val(),
                lastTerm = $.data(this.container, "select2-last-term"),
                // sequence number used to drop out-of-order responses
                queryNumber;

            // prevent duplicate queries against the same term
            if (initial !== true && lastTerm && equal(term, lastTerm)) return;

            $.data(this.container, "select2-last-term", term);

            // if the search is currently hidden we do not alter the results
            if (initial !== true && (this.showSearchInput === false || !this.opened())) {
                return;
            }

            function postRender() {
                search.removeClass("select2-active");
                self.positionDropdown();
                if (results.find('.select2-no-results,.select2-selection-limit,.select2-searching').length) {
                    self.liveRegion.text(results.text());
                }
                else {
                    self.liveRegion.text(self.opts.formatMatches(results.find('.select2-result-selectable').length));
                }
            }

            function render(html) {
                results.html(html);
                postRender();
            }

            queryNumber = ++this.queryCount;

            var maxSelSize = this.getMaximumSelectionSize();
            if (maxSelSize >=1) {
                data = this.data();
                if ($.isArray(data) && data.length >= maxSelSize && checkFormatter(opts.formatSelectionTooBig, "formatSelectionTooBig")) {
                    render("<li class='select2-selection-limit'>" + evaluate(opts.formatSelectionTooBig, maxSelSize) + "</li>");
                    return;
                }
            }

            if (search.val().length < opts.minimumInputLength) {
                if (checkFormatter(opts.formatInputTooShort, "formatInputTooShort")) {
                    render("<li class='select2-no-results'>" + evaluate(opts.formatInputTooShort, search.val(), opts.minimumInputLength) + "</li>");
                } else {
                    render("");
                }
                if (initial && this.showSearch) this.showSearch(true);
                return;
            }

            if (opts.maximumInputLength && search.val().length > opts.maximumInputLength) {
                if (checkFormatter(opts.formatInputTooLong, "formatInputTooLong")) {
                    render("<li class='select2-no-results'>" + evaluate(opts.formatInputTooLong, search.val(), opts.maximumInputLength) + "</li>");
                } else {
                    render("");
                }
                return;
            }

            if (opts.formatSearching && this.findHighlightableChoices().length === 0) {
                render("<li class='select2-searching'>" + evaluate(opts.formatSearching) + "</li>");
            }

            search.addClass("select2-active");

            this.removeHighlight();

            // give the tokenizer a chance to pre-process the input
            input = this.tokenize();
            if (input != undefined && input != null) {
                search.val(input);
            }

            this.resultsPage = 1;

            opts.query({
                element: opts.element,
                    term: search.val(),
                    page: this.resultsPage,
                    context: null,
                    matcher: opts.matcher,
                    callback: this.bind(function (data) {
                var def; // default choice

                // ignore old responses
                if (queryNumber != this.queryCount) {
                  return;
                }

                // ignore a response if the select2 has been closed before it was received
                if (!this.opened()) {
                    this.search.removeClass("select2-active");
                    return;
                }

                // save context, if any
                this.context = (data.context===undefined) ? null : data.context;
                // create a default choice and prepend it to the list
                if (this.opts.createSearchChoice && search.val() !== "") {
                    def = this.opts.createSearchChoice.call(self, search.val(), data.results);
                    if (def !== undefined && def !== null && self.id(def) !== undefined && self.id(def) !== null) {
                        if ($(data.results).filter(
                            function () {
                                return equal(self.id(this), self.id(def));
                            }).length === 0) {
                            this.opts.createSearchChoicePosition(data.results, def);
                        }
                    }
                }

                if (data.results.length === 0 && checkFormatter(opts.formatNoMatches, "formatNoMatches")) {
                    render("<li class='select2-no-results'>" + evaluate(opts.formatNoMatches, search.val()) + "</li>");
                    return;
                }

                results.empty();
                self.opts.populateResults.call(this, results, data.results, {term: search.val(), page: this.resultsPage, context:null});

                if (data.more === true && checkFormatter(opts.formatLoadMore, "formatLoadMore")) {
                    results.append("<li class='select2-more-results'>" + self.opts.escapeMarkup(evaluate(opts.formatLoadMore, this.resultsPage)) + "</li>");
                    window.setTimeout(function() { self.loadMoreIfNeeded(); }, 10);
                }

                this.postprocessResults(data, initial);

                postRender();

                this.opts.element.trigger({ type: "select2-loaded", items: data });
            })});
        },

        // abstract
        cancel: function () {
            this.close();
        },

        // abstract
        blur: function () {
            // if selectOnBlur == true, select the currently highlighted option
            if (this.opts.selectOnBlur)
                this.selectHighlighted({noFocus: true});

            this.close();
            this.container.removeClass("select2-container-active");
            // synonymous to .is(':focus'), which is available in jquery >= 1.6
            if (this.search[0] === document.activeElement) { this.search.blur(); }
            this.clearSearch();
            this.selection.find(".select2-search-choice-focus").removeClass("select2-search-choice-focus");
        },

        // abstract
        focusSearch: function () {
            focus(this.search);
        },

        // abstract
        selectHighlighted: function (options) {
            if (this._touchMoved) {
              this.clearTouchMoved();
              return;
            }
            var index=this.highlight(),
                highlighted=this.results.find(".select2-highlighted"),
                data = highlighted.closest('.select2-result').data("select2-data");

            if (data) {
                this.highlight(index);
                this.onSelect(data, options);
            } else if (options && options.noFocus) {
                this.close();
            }
        },

        // abstract
        getPlaceholder: function () {
            var placeholderOption;
            return this.opts.element.attr("placeholder") ||
                this.opts.element.attr("data-placeholder") || // jquery 1.4 compat
                this.opts.element.data("placeholder") ||
                this.opts.placeholder ||
                ((placeholderOption = this.getPlaceholderOption()) !== undefined ? placeholderOption.text() : undefined);
        },

        // abstract
        getPlaceholderOption: function() {
            if (this.select) {
                var firstOption = this.select.children('option').first();
                if (this.opts.placeholderOption !== undefined ) {
                    //Determine the placeholder option based on the specified placeholderOption setting
                    return (this.opts.placeholderOption === "first" && firstOption) ||
                           (typeof this.opts.placeholderOption === "function" && this.opts.placeholderOption(this.select));
                } else if ($.trim(firstOption.text()) === "" && firstOption.val() === "") {
                    //No explicit placeholder option specified, use the first if it's blank
                    return firstOption;
                }
            }
        },

        /**
         * Get the desired width for the container element.  This is
         * derived first from option `width` passed to select2, then
         * the inline 'style' on the original element, and finally
         * falls back to the jQuery calculated element width.
         */
        // abstract
        initContainerWidth: function () {
            function resolveContainerWidth() {
                var style, attrs, matches, i, l, attr;

                if (this.opts.width === "off") {
                    return null;
                } else if (this.opts.width === "element"){
                    return this.opts.element.outerWidth(false) === 0 ? 'auto' : this.opts.element.outerWidth(false) + 'px';
                } else if (this.opts.width === "copy" || this.opts.width === "resolve") {
                    // check if there is inline style on the element that contains width
                    style = this.opts.element.attr('style');
                    if (style !== undefined) {
                        attrs = style.split(';');
                        for (i = 0, l = attrs.length; i < l; i = i + 1) {
                            attr = attrs[i].replace(/\s/g, '');
                            matches = attr.match(/^width:(([-+]?([0-9]*\.)?[0-9]+)(px|em|ex|%|in|cm|mm|pt|pc))/i);
                            if (matches !== null && matches.length >= 1)
                                return matches[1];
                        }
                    }

                    if (this.opts.width === "resolve") {
                        // next check if css('width') can resolve a width that is percent based, this is sometimes possible
                        // when attached to input type=hidden or elements hidden via css
                        style = this.opts.element.css('width');
                        if (style.indexOf("%") > 0) return style;

                        // finally, fallback on the calculated width of the element
                        return (this.opts.element.outerWidth(false) === 0 ? 'auto' : this.opts.element.outerWidth(false) + 'px');
                    }

                    return null;
                } else if ($.isFunction(this.opts.width)) {
                    return this.opts.width();
                } else {
                    return this.opts.width;
               }
            };

            var width = resolveContainerWidth.call(this);
            if (width !== null) {
                this.container.css("width", width);
            }
        }
    });

    SingleSelect2 = clazz(AbstractSelect2, {

        // single

        createContainer: function () {
            var container = $(document.createElement("div")).attr({
                "class": "select2-container"
            }).html([
                "<a href='javascript:void(0)' class='select2-choice' tabindex='-1'>",
                "   <span class='select2-chosen'>&#160;</span><abbr class='select2-search-choice-close'></abbr>",
                "   <span class='select2-arrow' role='presentation'><b role='presentation'></b></span>",
                "</a>",
                "<label for='' class='select2-offscreen'></label>",
                "<input class='select2-focusser select2-offscreen' type='text' aria-haspopup='true' role='button' />",
                "<div class='select2-drop select2-display-none'>",
                "   <div class='select2-search'>",
                "       <label for='' class='select2-offscreen'></label>",
                "       <input type='text' autocomplete='off' autocorrect='off' autocapitalize='off' spellcheck='false' class='select2-input' role='combobox' aria-expanded='true'",
                "       aria-autocomplete='list' />",
                "   </div>",
                "   <ul class='select2-results' role='listbox'>",
                "   </ul>",
                "</div>"].join(""));
            return container;
        },

        // single
        enableInterface: function() {
            if (this.parent.enableInterface.apply(this, arguments)) {
                this.focusser.prop("disabled", !this.isInterfaceEnabled());
            }
        },

        // single
        opening: function () {
            var el, range, len;

            if (this.opts.minimumResultsForSearch >= 0) {
                this.showSearch(true);
            }

            this.parent.opening.apply(this, arguments);

            if (this.showSearchInput !== false) {
                // IE appends focusser.val() at the end of field :/ so we manually insert it at the beginning using a range
                // all other browsers handle this just fine

                this.search.val(this.focusser.val());
            }
            if (this.opts.shouldFocusInput(this)) {
                this.search.focus();
                // move the cursor to the end after focussing, otherwise it will be at the beginning and
                // new text will appear *before* focusser.val()
                el = this.search.get(0);
                if (el.createTextRange) {
                    range = el.createTextRange();
                    range.collapse(false);
                    range.select();
                } else if (el.setSelectionRange) {
                    len = this.search.val().length;
                    el.setSelectionRange(len, len);
                }
            }

            // initializes search's value with nextSearchTerm (if defined by user)
            // ignore nextSearchTerm if the dropdown is opened by the user pressing a letter
            if(this.search.val() === "") {
                if(this.nextSearchTerm != undefined){
                    this.search.val(this.nextSearchTerm);
                    this.search.select();
                }
            }

            this.focusser.prop("disabled", true).val("");
            this.updateResults(true);
            this.opts.element.trigger($.Event("select2-open"));
        },

        // single
        close: function () {
            if (!this.opened()) return;
            this.parent.close.apply(this, arguments);

            this.focusser.prop("disabled", false);

            if (this.opts.shouldFocusInput(this)) {
                this.focusser.focus();
            }
        },

        // single
        focus: function () {
            if (this.opened()) {
                this.close();
            } else {
                this.focusser.prop("disabled", false);
                if (this.opts.shouldFocusInput(this)) {
                    this.focusser.focus();
                }
            }
        },

        // single
        isFocused: function () {
            return this.container.hasClass("select2-container-active");
        },

        // single
        cancel: function () {
            this.parent.cancel.apply(this, arguments);
            this.focusser.prop("disabled", false);

            if (this.opts.shouldFocusInput(this)) {
                this.focusser.focus();
            }
        },

        // single
        destroy: function() {
            $("label[for='" + this.focusser.attr('id') + "']")
                .attr('for', this.opts.element.attr("id"));
            this.parent.destroy.apply(this, arguments);

            cleanupJQueryElements.call(this,
                "selection",
                "focusser"
            );
        },

        // single
        initContainer: function () {

            var selection,
                container = this.container,
                dropdown = this.dropdown,
                idSuffix = nextUid(),
                elementLabel;

            if (this.opts.minimumResultsForSearch < 0) {
                this.showSearch(false);
            } else {
                this.showSearch(true);
            }

            this.selection = selection = container.find(".select2-choice");

            this.focusser = container.find(".select2-focusser");

            // add aria associations
            selection.find(".select2-chosen").attr("id", "select2-chosen-"+idSuffix);
            this.focusser.attr("aria-labelledby", "select2-chosen-"+idSuffix);
            this.results.attr("id", "select2-results-"+idSuffix);
            this.search.attr("aria-owns", "select2-results-"+idSuffix);

            // rewrite labels from original element to focusser
            this.focusser.attr("id", "s2id_autogen"+idSuffix);

            elementLabel = $("label[for='" + this.opts.element.attr("id") + "']");

            this.focusser.prev()
                .text(elementLabel.text())
                .attr('for', this.focusser.attr('id'));

            // Ensure the original element retains an accessible name
            var originalTitle = this.opts.element.attr("title");
            this.opts.element.attr("title", (originalTitle || elementLabel.text()));

            this.focusser.attr("tabindex", this.elementTabIndex);

            // write label for search field using the label from the focusser element
            this.search.attr("id", this.focusser.attr('id') + '_search');

            this.search.prev()
                .text($("label[for='" + this.focusser.attr('id') + "']").text())
                .attr('for', this.search.attr('id'));

            this.search.on("keydown", this.bind(function (e) {
                if (!this.isInterfaceEnabled()) return;

                if (e.which === KEY.PAGE_UP || e.which === KEY.PAGE_DOWN) {
                    // prevent the page from scrolling
                    killEvent(e);
                    return;
                }

                switch (e.which) {
                    case KEY.UP:
                    case KEY.DOWN:
                        this.moveHighlight((e.which === KEY.UP) ? -1 : 1);
                        killEvent(e);
                        return;
                    case KEY.ENTER:
                        this.selectHighlighted();
                        killEvent(e);
                        return;
                    case KEY.TAB:
                        this.selectHighlighted({noFocus: true});
                        return;
                    case KEY.ESC:
                        this.cancel(e);
                        killEvent(e);
                        return;
                }
            }));

            this.search.on("blur", this.bind(function(e) {
                // a workaround for chrome to keep the search field focussed when the scroll bar is used to scroll the dropdown.
                // without this the search field loses focus which is annoying
                if (document.activeElement === this.body.get(0)) {
                    window.setTimeout(this.bind(function() {
                        if (this.opened()) {
                            this.search.focus();
                        }
                    }), 0);
                }
            }));

            this.focusser.on("keydown", this.bind(function (e) {
                if (!this.isInterfaceEnabled()) return;

                if (e.which === KEY.TAB || KEY.isControl(e) || KEY.isFunctionKey(e) || e.which === KEY.ESC) {
                    return;
                }

                if (this.opts.openOnEnter === false && e.which === KEY.ENTER) {
                    killEvent(e);
                    return;
                }

                if (e.which == KEY.DOWN || e.which == KEY.UP
                    || (e.which == KEY.ENTER && this.opts.openOnEnter)) {

                    if (e.altKey || e.ctrlKey || e.shiftKey || e.metaKey) return;

                    this.open();
                    killEvent(e);
                    return;
                }

                if (e.which == KEY.DELETE || e.which == KEY.BACKSPACE) {
                    if (this.opts.allowClear) {
                        this.clear();
                    }
                    killEvent(e);
                    return;
                }
            }));


            installKeyUpChangeEvent(this.focusser);
            this.focusser.on("keyup-change input", this.bind(function(e) {
                if (this.opts.minimumResultsForSearch >= 0) {
                    e.stopPropagation();
                    if (this.opened()) return;
                    this.open();
                }
            }));

            selection.on("mousedown touchstart", "abbr", this.bind(function (e) {
                if (!this.isInterfaceEnabled()) return;
                this.clear();
                killEventImmediately(e);
                this.close();
                this.selection.focus();
            }));

            selection.on("mousedown touchstart", this.bind(function (e) {
                // Prevent IE from generating a click event on the body
                reinsertElement(selection);

                if (!this.container.hasClass("select2-container-active")) {
                    this.opts.element.trigger($.Event("select2-focus"));
                }

                if (this.opened()) {
                    this.close();
                } else if (this.isInterfaceEnabled()) {
                    this.open();
                }

                killEvent(e);
            }));

            dropdown.on("mousedown touchstart", this.bind(function() {
                if (this.opts.shouldFocusInput(this)) {
                    this.search.focus();
                }
            }));

            selection.on("focus", this.bind(function(e) {
                killEvent(e);
            }));

            this.focusser.on("focus", this.bind(function(){
                if (!this.container.hasClass("select2-container-active")) {
                    this.opts.element.trigger($.Event("select2-focus"));
                }
                this.container.addClass("select2-container-active");
            })).on("blur", this.bind(function() {
                if (!this.opened()) {
                    this.container.removeClass("select2-container-active");
                    this.opts.element.trigger($.Event("select2-blur"));
                }
            }));
            this.search.on("focus", this.bind(function(){
                if (!this.container.hasClass("select2-container-active")) {
                    this.opts.element.trigger($.Event("select2-focus"));
                }
                this.container.addClass("select2-container-active");
            }));

            this.initContainerWidth();
            this.opts.element.addClass("select2-offscreen");
            this.setPlaceholder();

        },

        // single
        clear: function(triggerChange) {
            var data=this.selection.data("select2-data");
            if (data) { // guard against queued quick consecutive clicks
                var evt = $.Event("select2-clearing");
                this.opts.element.trigger(evt);
                if (evt.isDefaultPrevented()) {
                    return;
                }
                var placeholderOption = this.getPlaceholderOption();
                this.opts.element.val(placeholderOption ? placeholderOption.val() : "");
                this.selection.find(".select2-chosen").empty();
                this.selection.removeData("select2-data");
                this.setPlaceholder();

                if (triggerChange !== false){
                    this.opts.element.trigger({ type: "select2-removed", val: this.id(data), choice: data });
                    this.triggerChange({removed:data});
                }
            }
        },

        /**
         * Sets selection based on source element's value
         */
        // single
        initSelection: function () {
            var selected;
            if (this.isPlaceholderOptionSelected()) {
                this.updateSelection(null);
                this.close();
                this.setPlaceholder();
            } else {
                var self = this;
                this.opts.initSelection.call(null, this.opts.element, function(selected){
                    if (selected !== undefined && selected !== null) {
                        self.updateSelection(selected);
                        self.close();
                        self.setPlaceholder();
                        self.nextSearchTerm = self.opts.nextSearchTerm(selected, self.search.val());
                    }
                });
            }
        },

        isPlaceholderOptionSelected: function() {
            var placeholderOption;
            if (this.getPlaceholder() === undefined) return false; // no placeholder specified so no option should be considered
            return ((placeholderOption = this.getPlaceholderOption()) !== undefined && placeholderOption.prop("selected"))
                || (this.opts.element.val() === "")
                || (this.opts.element.val() === undefined)
                || (this.opts.element.val() === null);
        },

        // single
        prepareOpts: function () {
            var opts = this.parent.prepareOpts.apply(this, arguments),
                self=this;

            if (opts.element.get(0).tagName.toLowerCase() === "select") {
                // install the selection initializer
                opts.initSelection = function (element, callback) {
                    var selected = element.find("option").filter(function() { return this.selected && !this.disabled });
                    // a single select box always has a value, no need to null check 'selected'
                    callback(self.optionToData(selected));
                };
            } else if ("data" in opts) {
                // install default initSelection when applied to hidden input and data is local
                opts.initSelection = opts.initSelection || function (element, callback) {
                    var id = element.val();
                    //search in data by id, storing the actual matching item
                    var match = null;
                    opts.query({
                        matcher: function(term, text, el){
                            var is_match = equal(id, opts.id(el));
                            if (is_match) {
                                match = el;
                            }
                            return is_match;
                        },
                        callback: !$.isFunction(callback) ? $.noop : function() {
                            callback(match);
                        }
                    });
                };
            }

            return opts;
        },

        // single
        getPlaceholder: function() {
            // if a placeholder is specified on a single select without a valid placeholder option ignore it
            if (this.select) {
                if (this.getPlaceholderOption() === undefined) {
                    return undefined;
                }
            }

            return this.parent.getPlaceholder.apply(this, arguments);
        },

        // single
        setPlaceholder: function () {
            var placeholder = this.getPlaceholder();

            if (this.isPlaceholderOptionSelected() && placeholder !== undefined) {

                // check for a placeholder option if attached to a select
                if (this.select && this.getPlaceholderOption() === undefined) return;

                this.selection.find(".select2-chosen").html(this.opts.escapeMarkup(placeholder));

                this.selection.addClass("select2-default");

                this.container.removeClass("select2-allowclear");
            }
        },

        // single
        postprocessResults: function (data, initial, noHighlightUpdate) {
            var selected = 0, self = this, showSearchInput = true;

            // find the selected element in the result list

            this.findHighlightableChoices().each2(function (i, elm) {
                if (equal(self.id(elm.data("select2-data")), self.opts.element.val())) {
                    selected = i;
                    return false;
                }
            });

            // and highlight it
            if (noHighlightUpdate !== false) {
                if (initial === true && selected >= 0) {
                    this.highlight(selected);
                } else {
                    this.highlight(0);
                }
            }

            // hide the search box if this is the first we got the results and there are enough of them for search

            if (initial === true) {
                var min = this.opts.minimumResultsForSearch;
                if (min >= 0) {
                    this.showSearch(countResults(data.results) >= min);
                }
            }
        },

        // single
        showSearch: function(showSearchInput) {
            if (this.showSearchInput === showSearchInput) return;

            this.showSearchInput = showSearchInput;

            this.dropdown.find(".select2-search").toggleClass("select2-search-hidden", !showSearchInput);
            this.dropdown.find(".select2-search").toggleClass("select2-offscreen", !showSearchInput);
            //add "select2-with-searchbox" to the container if search box is shown
            $(this.dropdown, this.container).toggleClass("select2-with-searchbox", showSearchInput);
        },

        // single
        onSelect: function (data, options) {

            if (!this.triggerSelect(data)) { return; }

            var old = this.opts.element.val(),
                oldData = this.data();

            this.opts.element.val(this.id(data));
            this.updateSelection(data);

            this.opts.element.trigger({ type: "select2-selected", val: this.id(data), choice: data });

            this.nextSearchTerm = this.opts.nextSearchTerm(data, this.search.val());
            this.close();

            if ((!options || !options.noFocus) && this.opts.shouldFocusInput(this)) {
                this.focusser.focus();
            }

            if (!equal(old, this.id(data))) {
                this.triggerChange({ added: data, removed: oldData });
            }
        },

        // single
        updateSelection: function (data) {

            var container=this.selection.find(".select2-chosen"), formatted, cssClass;

            this.selection.data("select2-data", data);

            container.empty();
            if (data !== null) {
                formatted=this.opts.formatSelection(data, container, this.opts.escapeMarkup);
            }
            if (formatted !== undefined) {
                container.append(formatted);
            }
            cssClass=this.opts.formatSelectionCssClass(data, container);
            if (cssClass !== undefined) {
                container.addClass(cssClass);
            }

            this.selection.removeClass("select2-default");

            if (this.opts.allowClear && this.getPlaceholder() !== undefined) {
                this.container.addClass("select2-allowclear");
            }
        },

        // single
        val: function () {
            var val,
                triggerChange = false,
                data = null,
                self = this,
                oldData = this.data();

            if (arguments.length === 0) {
                return this.opts.element.val();
            }

            val = arguments[0];

            if (arguments.length > 1) {
                triggerChange = arguments[1];
            }

            if (this.select) {
                this.select
                    .val(val)
                    .find("option").filter(function() { return this.selected }).each2(function (i, elm) {
                        data = self.optionToData(elm);
                        return false;
                    });
                this.updateSelection(data);
                this.setPlaceholder();
                if (triggerChange) {
                    this.triggerChange({added: data, removed:oldData});
                }
            } else {
                // val is an id. !val is true for [undefined,null,'',0] - 0 is legal
                if (!val && val !== 0) {
                    this.clear(triggerChange);
                    return;
                }
                if (this.opts.initSelection === undefined) {
                    throw new Error("cannot call val() if initSelection() is not defined");
                }
                this.opts.element.val(val);
                this.opts.initSelection(this.opts.element, function(data){
                    self.opts.element.val(!data ? "" : self.id(data));
                    self.updateSelection(data);
                    self.setPlaceholder();
                    if (triggerChange) {
                        self.triggerChange({added: data, removed:oldData});
                    }
                });
            }
        },

        // single
        clearSearch: function () {
            this.search.val("");
            this.focusser.val("");
        },

        // single
        data: function(value) {
            var data,
                triggerChange = false;

            if (arguments.length === 0) {
                data = this.selection.data("select2-data");
                if (data == undefined) data = null;
                return data;
            } else {
                if (arguments.length > 1) {
                    triggerChange = arguments[1];
                }
                if (!value) {
                    this.clear(triggerChange);
                } else {
                    data = this.data();
                    this.opts.element.val(!value ? "" : this.id(value));
                    this.updateSelection(value);
                    if (triggerChange) {
                        this.triggerChange({added: value, removed:data});
                    }
                }
            }
        }
    });

    MultiSelect2 = clazz(AbstractSelect2, {

        // multi
        createContainer: function () {
            var container = $(document.createElement("div")).attr({
                "class": "select2-container select2-container-multi"
            }).html([
                "<ul class='select2-choices'>",
                "  <li class='select2-search-field'>",
                "    <label for='' class='select2-offscreen'></label>",
                "    <input type='text' autocomplete='off' autocorrect='off' autocapitalize='off' spellcheck='false' class='select2-input'>",
                "  </li>",
                "</ul>",
                "<div class='select2-drop select2-drop-multi select2-display-none'>",
                "   <ul class='select2-results'>",
                "   </ul>",
                "</div>"].join(""));
            return container;
        },

        // multi
        prepareOpts: function () {
            var opts = this.parent.prepareOpts.apply(this, arguments),
                self=this;

            // TODO validate placeholder is a string if specified

            if (opts.element.get(0).tagName.toLowerCase() === "select") {
                // install the selection initializer
                opts.initSelection = function (element, callback) {

                    var data = [];

                    element.find("option").filter(function() { return this.selected && !this.disabled }).each2(function (i, elm) {
                        data.push(self.optionToData(elm));
                    });
                    callback(data);
                };
            } else if ("data" in opts) {
                // install default initSelection when applied to hidden input and data is local
                opts.initSelection = opts.initSelection || function (element, callback) {
                    var ids = splitVal(element.val(), opts.separator);
                    //search in data by array of ids, storing matching items in a list
                    var matches = [];
                    opts.query({
                        matcher: function(term, text, el){
                            var is_match = $.grep(ids, function(id) {
                                return equal(id, opts.id(el));
                            }).length;
                            if (is_match) {
                                matches.push(el);
                            }
                            return is_match;
                        },
                        callback: !$.isFunction(callback) ? $.noop : function() {
                            // reorder matches based on the order they appear in the ids array because right now
                            // they are in the order in which they appear in data array
                            var ordered = [];
                            for (var i = 0; i < ids.length; i++) {
                                var id = ids[i];
                                for (var j = 0; j < matches.length; j++) {
                                    var match = matches[j];
                                    if (equal(id, opts.id(match))) {
                                        ordered.push(match);
                                        matches.splice(j, 1);
                                        break;
                                    }
                                }
                            }
                            callback(ordered);
                        }
                    });
                };
            }

            return opts;
        },

        // multi
        selectChoice: function (choice) {

            var selected = this.container.find(".select2-search-choice-focus");
            if (selected.length && choice && choice[0] == selected[0]) {

            } else {
                if (selected.length) {
                    this.opts.element.trigger("choice-deselected", selected);
                }
                selected.removeClass("select2-search-choice-focus");
                if (choice && choice.length) {
                    this.close();
                    choice.addClass("select2-search-choice-focus");
                    this.opts.element.trigger("choice-selected", choice);
                }
            }
        },

        // multi
        destroy: function() {
            $("label[for='" + this.search.attr('id') + "']")
                .attr('for', this.opts.element.attr("id"));
            this.parent.destroy.apply(this, arguments);

            cleanupJQueryElements.call(this,
                "searchContainer",
                "selection"
            );
        },

        // multi
        initContainer: function () {

            var selector = ".select2-choices", selection;

            this.searchContainer = this.container.find(".select2-search-field");
            this.selection = selection = this.container.find(selector);

            var _this = this;
            this.selection.on("click", ".select2-search-choice:not(.select2-locked)", function (e) {
                //killEvent(e);
                _this.search[0].focus();
                _this.selectChoice($(this));
            });

            // rewrite labels from original element to focusser
            this.search.attr("id", "s2id_autogen"+nextUid());

            this.search.prev()
                .text($("label[for='" + this.opts.element.attr("id") + "']").text())
                .attr('for', this.search.attr('id'));

            this.search.on("input paste", this.bind(function() {
                if (!this.isInterfaceEnabled()) return;
                if (!this.opened()) {
                    this.open();
                }
            }));

            this.search.attr("tabindex", this.elementTabIndex);

            this.keydowns = 0;
            this.search.on("keydown", this.bind(function (e) {
                if (!this.isInterfaceEnabled()) return;

                ++this.keydowns;
                var selected = selection.find(".select2-search-choice-focus");
                var prev = selected.prev(".select2-search-choice:not(.select2-locked)");
                var next = selected.next(".select2-search-choice:not(.select2-locked)");
                var pos = getCursorInfo(this.search);

                if (selected.length &&
                    (e.which == KEY.LEFT || e.which == KEY.RIGHT || e.which == KEY.BACKSPACE || e.which == KEY.DELETE || e.which == KEY.ENTER)) {
                    var selectedChoice = selected;
                    if (e.which == KEY.LEFT && prev.length) {
                        selectedChoice = prev;
                    }
                    else if (e.which == KEY.RIGHT) {
                        selectedChoice = next.length ? next : null;
                    }
                    else if (e.which === KEY.BACKSPACE) {
                        if (this.unselect(selected.first())) {
                            this.search.width(10);
                            selectedChoice = prev.length ? prev : next;
                        }
                    } else if (e.which == KEY.DELETE) {
                        if (this.unselect(selected.first())) {
                            this.search.width(10);
                            selectedChoice = next.length ? next : null;
                        }
                    } else if (e.which == KEY.ENTER) {
                        selectedChoice = null;
                    }

                    this.selectChoice(selectedChoice);
                    killEvent(e);
                    if (!selectedChoice || !selectedChoice.length) {
                        this.open();
                    }
                    return;
                } else if (((e.which === KEY.BACKSPACE && this.keydowns == 1)
                    || e.which == KEY.LEFT) && (pos.offset == 0 && !pos.length)) {

                    this.selectChoice(selection.find(".select2-search-choice:not(.select2-locked)").last());
                    killEvent(e);
                    return;
                } else {
                    this.selectChoice(null);
                }

                if (this.opened()) {
                    switch (e.which) {
                    case KEY.UP:
                    case KEY.DOWN:
                        this.moveHighlight((e.which === KEY.UP) ? -1 : 1);
                        killEvent(e);
                        return;
                    case KEY.ENTER:
                        this.selectHighlighted();
                        killEvent(e);
                        return;
                    case KEY.TAB:
                        this.selectHighlighted({noFocus:true});
                        this.close();
                        return;
                    case KEY.ESC:
                        this.cancel(e);
                        killEvent(e);
                        return;
                    }
                }

                if (e.which === KEY.TAB || KEY.isControl(e) || KEY.isFunctionKey(e)
                 || e.which === KEY.BACKSPACE || e.which === KEY.ESC) {
                    return;
                }

                if (e.which === KEY.ENTER) {
                    if (this.opts.openOnEnter === false) {
                        return;
                    } else if (e.altKey || e.ctrlKey || e.shiftKey || e.metaKey) {
                        return;
                    }
                }

                this.open();

                if (e.which === KEY.PAGE_UP || e.which === KEY.PAGE_DOWN) {
                    // prevent the page from scrolling
                    killEvent(e);
                }

                if (e.which === KEY.ENTER) {
                    // prevent form from being submitted
                    killEvent(e);
                }

            }));

            this.search.on("keyup", this.bind(function (e) {
                this.keydowns = 0;
                this.resizeSearch();
            })
            );

            this.search.on("blur", this.bind(function(e) {
                this.container.removeClass("select2-container-active");
                this.search.removeClass("select2-focused");
                this.selectChoice(null);
                if (!this.opened()) this.clearSearch();
                e.stopImmediatePropagation();
                this.opts.element.trigger($.Event("select2-blur"));
            }));

            this.container.on("click", selector, this.bind(function (e) {
                if (!this.isInterfaceEnabled()) return;
                if ($(e.target).closest(".select2-search-choice").length > 0) {
                    // clicked inside a select2 search choice, do not open
                    return;
                }
                this.selectChoice(null);
                this.clearPlaceholder();
                if (!this.container.hasClass("select2-container-active")) {
                    this.opts.element.trigger($.Event("select2-focus"));
                }
                this.open();
                this.focusSearch();
                e.preventDefault();
            }));

            this.container.on("focus", selector, this.bind(function () {
                if (!this.isInterfaceEnabled()) return;
                if (!this.container.hasClass("select2-container-active")) {
                    this.opts.element.trigger($.Event("select2-focus"));
                }
                this.container.addClass("select2-container-active");
                this.dropdown.addClass("select2-drop-active");
                this.clearPlaceholder();
            }));

            this.initContainerWidth();
            this.opts.element.addClass("select2-offscreen");

            // set the placeholder if necessary
            this.clearSearch();
        },

        // multi
        enableInterface: function() {
            if (this.parent.enableInterface.apply(this, arguments)) {
                this.search.prop("disabled", !this.isInterfaceEnabled());
            }
        },

        // multi
        initSelection: function () {
            var data;
            if (this.opts.element.val() === "" && this.opts.element.text() === "") {
                this.updateSelection([]);
                this.close();
                // set the placeholder if necessary
                this.clearSearch();
            }
            if (this.select || this.opts.element.val() !== "") {
                var self = this;
                this.opts.initSelection.call(null, this.opts.element, function(data){
                    if (data !== undefined && data !== null) {
                        self.updateSelection(data);
                        self.close();
                        // set the placeholder if necessary
                        self.clearSearch();
                    }
                });
            }
        },

        // multi
        clearSearch: function () {
            var placeholder = this.getPlaceholder(),
                maxWidth = this.getMaxSearchWidth();

            if (placeholder !== undefined  && this.getVal().length === 0 && this.search.hasClass("select2-focused") === false) {
                this.search.val(placeholder).addClass("select2-default");
                // stretch the search box to full width of the container so as much of the placeholder is visible as possible
                // we could call this.resizeSearch(), but we do not because that requires a sizer and we do not want to create one so early because of a firefox bug, see #944
                this.search.width(maxWidth > 0 ? maxWidth : this.container.css("width"));
            } else {
                this.search.val("").width(10);
            }
        },

        // multi
        clearPlaceholder: function () {
            if (this.search.hasClass("select2-default")) {
                this.search.val("").removeClass("select2-default");
            }
        },

        // multi
        opening: function () {
            this.clearPlaceholder(); // should be done before super so placeholder is not used to search
            this.resizeSearch();

            this.parent.opening.apply(this, arguments);

            this.focusSearch();

            // initializes search's value with nextSearchTerm (if defined by user)
            // ignore nextSearchTerm if the dropdown is opened by the user pressing a letter
            if(this.search.val() === "") {
                if(this.nextSearchTerm != undefined){
                    this.search.val(this.nextSearchTerm);
                    this.search.select();
                }
            }

            this.updateResults(true);
            if (this.opts.shouldFocusInput(this)) {
                this.search.focus();
            }
            this.opts.element.trigger($.Event("select2-open"));
        },

        // multi
        close: function () {
            if (!this.opened()) return;
            this.parent.close.apply(this, arguments);
        },

        // multi
        focus: function () {
            this.close();
            this.search.focus();
        },

        // multi
        isFocused: function () {
            return this.search.hasClass("select2-focused");
        },

        // multi
        updateSelection: function (data) {
            var ids = [], filtered = [], self = this;

            // filter out duplicates
            $(data).each(function () {
                if (indexOf(self.id(this), ids) < 0) {
                    ids.push(self.id(this));
                    filtered.push(this);
                }
            });
            data = filtered;

            this.selection.find(".select2-search-choice").remove();
            $(data).each(function () {
                self.addSelectedChoice(this);
            });
            self.postprocessResults();
        },

        // multi
        tokenize: function() {
            var input = this.search.val();
            input = this.opts.tokenizer.call(this, input, this.data(), this.bind(this.onSelect), this.opts);
            if (input != null && input != undefined) {
                this.search.val(input);
                if (input.length > 0) {
                    this.open();
                }
            }

        },

        // multi
        onSelect: function (data, options) {

            if (!this.triggerSelect(data)) { return; }

            this.addSelectedChoice(data);

            this.opts.element.trigger({ type: "selected", val: this.id(data), choice: data });

            // keep track of the search's value before it gets cleared
            this.nextSearchTerm = this.opts.nextSearchTerm(data, this.search.val());

            this.clearSearch();
            this.updateResults();

            if (this.select || !this.opts.closeOnSelect) this.postprocessResults(data, false, this.opts.closeOnSelect===true);

            if (this.opts.closeOnSelect) {
                this.close();
                this.search.width(10);
            } else {
                if (this.countSelectableResults()>0) {
                    this.search.width(10);
                    this.resizeSearch();
                    if (this.getMaximumSelectionSize() > 0 && this.val().length >= this.getMaximumSelectionSize()) {
                        // if we reached max selection size repaint the results so choices
                        // are replaced with the max selection reached message
                        this.updateResults(true);
                    } else {
                        // initializes search's value with nextSearchTerm and update search result
                        if(this.nextSearchTerm != undefined){
                            this.search.val(this.nextSearchTerm);
                            this.updateResults();
                            this.search.select();
                        }
                    }
                    this.positionDropdown();
                } else {
                    // if nothing left to select close
                    this.close();
                    this.search.width(10);
                }
            }

            // since its not possible to select an element that has already been
            // added we do not need to check if this is a new element before firing change
            this.triggerChange({ added: data });

            if (!options || !options.noFocus)
                this.focusSearch();
        },

        // multi
        cancel: function () {
            this.close();
            this.focusSearch();
        },

        addSelectedChoice: function (data) {
            var enableChoice = !data.locked,
                enabledItem = $(
                    "<li class='select2-search-choice'>" +
                    "    <div></div>" +
                    "    <a href='#' class='select2-search-choice-close' tabindex='-1'></a>" +
                    "</li>"),
                disabledItem = $(
                    "<li class='select2-search-choice select2-locked'>" +
                    "<div></div>" +
                    "</li>");
            var choice = enableChoice ? enabledItem : disabledItem,
                id = this.id(data),
                val = this.getVal(),
                formatted,
                cssClass;

            formatted=this.opts.formatSelection(data, choice.find("div"), this.opts.escapeMarkup);
            if (formatted != undefined) {
                choice.find("div").replaceWith("<div>"+formatted+"</div>");
            }
            cssClass=this.opts.formatSelectionCssClass(data, choice.find("div"));
            if (cssClass != undefined) {
                choice.addClass(cssClass);
            }

            if(enableChoice){
              choice.find(".select2-search-choice-close")
                  .on("mousedown", killEvent)
                  .on("click dblclick", this.bind(function (e) {
                  if (!this.isInterfaceEnabled()) return;

                  this.unselect($(e.target));
                  this.selection.find(".select2-search-choice-focus").removeClass("select2-search-choice-focus");
                  killEvent(e);
                  this.close();
                  this.focusSearch();
              })).on("focus", this.bind(function () {
                  if (!this.isInterfaceEnabled()) return;
                  this.container.addClass("select2-container-active");
                  this.dropdown.addClass("select2-drop-active");
              }));
            }

            choice.data("select2-data", data);
            choice.insertBefore(this.searchContainer);

            val.push(id);
            this.setVal(val);
        },

        // multi
        unselect: function (selected) {
            var val = this.getVal(),
                data,
                index;
            selected = selected.closest(".select2-search-choice");

            if (selected.length === 0) {
                throw "Invalid argument: " + selected + ". Must be .select2-search-choice";
            }

            data = selected.data("select2-data");

            if (!data) {
                // prevent a race condition when the 'x' is clicked really fast repeatedly the event can be queued
                // and invoked on an element already removed
                return;
            }

            var evt = $.Event("select2-removing");
            evt.val = this.id(data);
            evt.choice = data;
            this.opts.element.trigger(evt);

            if (evt.isDefaultPrevented()) {
                return false;
            }

            while((index = indexOf(this.id(data), val)) >= 0) {
                val.splice(index, 1);
                this.setVal(val);
                if (this.select) this.postprocessResults();
            }

            selected.remove();

            this.opts.element.trigger({ type: "select2-removed", val: this.id(data), choice: data });
            this.triggerChange({ removed: data });

            return true;
        },

        // multi
        postprocessResults: function (data, initial, noHighlightUpdate) {
            var val = this.getVal(),
                choices = this.results.find(".select2-result"),
                compound = this.results.find(".select2-result-with-children"),
                self = this;

            choices.each2(function (i, choice) {
                var id = self.id(choice.data("select2-data"));
                if (indexOf(id, val) >= 0) {
                    choice.addClass("select2-selected");
                    // mark all children of the selected parent as selected
                    choice.find(".select2-result-selectable").addClass("select2-selected");
                }
            });

            compound.each2(function(i, choice) {
                // hide an optgroup if it doesn't have any selectable children
                if (!choice.is('.select2-result-selectable')
                    && choice.find(".select2-result-selectable:not(.select2-selected)").length === 0) {
                    choice.addClass("select2-selected");
                }
            });

            if (this.highlight() == -1 && noHighlightUpdate !== false){
                self.highlight(0);
            }

            //If all results are chosen render formatNoMatches
            if(!this.opts.createSearchChoice && !choices.filter('.select2-result:not(.select2-selected)').length > 0){
                if(!data || data && !data.more && this.results.find(".select2-no-results").length === 0) {
                    if (checkFormatter(self.opts.formatNoMatches, "formatNoMatches")) {
                        this.results.append("<li class='select2-no-results'>" + evaluate(self.opts.formatNoMatches, self.search.val()) + "</li>");
                    }
                }
            }

        },

        // multi
        getMaxSearchWidth: function() {
            return this.selection.width() - getSideBorderPadding(this.search);
        },

        // multi
        resizeSearch: function () {
            var minimumWidth, left, maxWidth, containerLeft, searchWidth,
                sideBorderPadding = getSideBorderPadding(this.search);

            minimumWidth = measureTextWidth(this.search) + 10;

            left = this.search.offset().left;

            maxWidth = this.selection.width();
            containerLeft = this.selection.offset().left;

            searchWidth = maxWidth - (left - containerLeft) - sideBorderPadding;

            if (searchWidth < minimumWidth) {
                searchWidth = maxWidth - sideBorderPadding;
            }

            if (searchWidth < 40) {
                searchWidth = maxWidth - sideBorderPadding;
            }

            if (searchWidth <= 0) {
              searchWidth = minimumWidth;
            }

            this.search.width(Math.floor(searchWidth));
        },

        // multi
        getVal: function () {
            var val;
            if (this.select) {
                val = this.select.val();
                return val === null ? [] : val;
            } else {
                val = this.opts.element.val();
                return splitVal(val, this.opts.separator);
            }
        },

        // multi
        setVal: function (val) {
            var unique;
            if (this.select) {
                this.select.val(val);
            } else {
                unique = [];
                // filter out duplicates
                $(val).each(function () {
                    if (indexOf(this, unique) < 0) unique.push(this);
                });
                this.opts.element.val(unique.length === 0 ? "" : unique.join(this.opts.separator));
            }
        },

        // multi
        buildChangeDetails: function (old, current) {
            var current = current.slice(0),
                old = old.slice(0);

            // remove intersection from each array
            for (var i = 0; i < current.length; i++) {
                for (var j = 0; j < old.length; j++) {
                    if (equal(this.opts.id(current[i]), this.opts.id(old[j]))) {
                        current.splice(i, 1);
                        if(i>0){
                        	i--;
                        }
                        old.splice(j, 1);
                        j--;
                    }
                }
            }

            return {added: current, removed: old};
        },


        // multi
        val: function (val, triggerChange) {
            var oldData, self=this;

            if (arguments.length === 0) {
                return this.getVal();
            }

            oldData=this.data();
            if (!oldData.length) oldData=[];

            // val is an id. !val is true for [undefined,null,'',0] - 0 is legal
            if (!val && val !== 0) {
                this.opts.element.val("");
                this.updateSelection([]);
                this.clearSearch();
                if (triggerChange) {
                    this.triggerChange({added: this.data(), removed: oldData});
                }
                return;
            }

            // val is a list of ids
            this.setVal(val);

            if (this.select) {
                this.opts.initSelection(this.select, this.bind(this.updateSelection));
                if (triggerChange) {
                    this.triggerChange(this.buildChangeDetails(oldData, this.data()));
                }
            } else {
                if (this.opts.initSelection === undefined) {
                    throw new Error("val() cannot be called if initSelection() is not defined");
                }

                this.opts.initSelection(this.opts.element, function(data){
                    var ids=$.map(data, self.id);
                    self.setVal(ids);
                    self.updateSelection(data);
                    self.clearSearch();
                    if (triggerChange) {
                        self.triggerChange(self.buildChangeDetails(oldData, self.data()));
                    }
                });
            }
            this.clearSearch();
        },

        // multi
        onSortStart: function() {
            if (this.select) {
                throw new Error("Sorting of elements is not supported when attached to <select>. Attach to <input type='hidden'/> instead.");
            }

            // collapse search field into 0 width so its container can be collapsed as well
            this.search.width(0);
            // hide the container
            this.searchContainer.hide();
        },

        // multi
        onSortEnd:function() {

            var val=[], self=this;

            // show search and move it to the end of the list
            this.searchContainer.show();
            // make sure the search container is the last item in the list
            this.searchContainer.appendTo(this.searchContainer.parent());
            // since we collapsed the width in dragStarted, we resize it here
            this.resizeSearch();

            // update selection
            this.selection.find(".select2-search-choice").each(function() {
                val.push(self.opts.id($(this).data("select2-data")));
            });
            this.setVal(val);
            this.triggerChange();
        },

        // multi
        data: function(values, triggerChange) {
            var self=this, ids, old;
            if (arguments.length === 0) {
                 return this.selection
                     .children(".select2-search-choice")
                     .map(function() { return $(this).data("select2-data"); })
                     .get();
            } else {
                old = this.data();
                if (!values) { values = []; }
                ids = $.map(values, function(e) { return self.opts.id(e); });
                this.setVal(ids);
                this.updateSelection(values);
                this.clearSearch();
                if (triggerChange) {
                    this.triggerChange(this.buildChangeDetails(old, this.data()));
                }
            }
        }
    });

    $.fn.select2 = function () {

        var args = Array.prototype.slice.call(arguments, 0),
            opts,
            select2,
            method, value, multiple,
            allowedMethods = ["val", "destroy", "opened", "open", "close", "focus", "isFocused", "container", "dropdown", "onSortStart", "onSortEnd", "enable", "disable", "readonly", "positionDropdown", "data", "search"],
            valueMethods = ["opened", "isFocused", "container", "dropdown"],
            propertyMethods = ["val", "data"],
            methodsMap = { search: "externalSearch" };

        this.each(function () {
            if (args.length === 0 || typeof(args[0]) === "object") {
                opts = args.length === 0 ? {} : $.extend({}, args[0]);
                opts.element = $(this);

                if (opts.element.get(0).tagName.toLowerCase() === "select") {
                    multiple = opts.element.prop("multiple");
                } else {
                    multiple = opts.multiple || false;
                    if ("tags" in opts) {opts.multiple = multiple = true;}
                }

                select2 = multiple ? new window.Select2["class"].multi() : new window.Select2["class"].single();
                select2.init(opts);
            } else if (typeof(args[0]) === "string") {

                if (indexOf(args[0], allowedMethods) < 0) {
                    throw "Unknown method: " + args[0];
                }

                value = undefined;
                select2 = $(this).data("select2");
                if (select2 === undefined) return;

                method=args[0];

                if (method === "container") {
                    value = select2.container;
                } else if (method === "dropdown") {
                    value = select2.dropdown;
                } else {
                    if (methodsMap[method]) method = methodsMap[method];

                    value = select2[method].apply(select2, args.slice(1));
                }
                if (indexOf(args[0], valueMethods) >= 0
                    || (indexOf(args[0], propertyMethods) >= 0 && args.length == 1)) {
                    return false; // abort the iteration, ready to return first matched value
                }
            } else {
                throw "Invalid arguments to select2 plugin: " + args;
            }
        });
        return (value === undefined) ? this : value;
    };

    // plugin defaults, accessible to users
    $.fn.select2.defaults = {
        width: "copy",
        loadMorePadding: 0,
        closeOnSelect: true,
        openOnEnter: true,
        containerCss: {},
        dropdownCss: {},
        containerCssClass: "",
        dropdownCssClass: "",
        formatResult: function(result, container, query, escapeMarkup) {
            var markup=[];
            markMatch(result.text, query.term, markup, escapeMarkup);
            return markup.join("");
        },
        formatSelection: function (data, container, escapeMarkup) {
            return data ? escapeMarkup(data.text) : undefined;
        },
        sortResults: function (results, container, query) {
            return results;
        },
        formatResultCssClass: function(data) {return data.css;},
        formatSelectionCssClass: function(data, container) {return undefined;},
        formatMatches: function (matches) { return matches + " results are available, use up and down arrow keys to navigate."; },
        formatNoMatches: function () { return "No matches found"; },
        formatInputTooShort: function (input, min) { var n = min - input.length; return "Please enter " + n + " or more character" + (n == 1? "" : "s"); },
        formatInputTooLong: function (input, max) { var n = input.length - max; return "Please delete " + n + " character" + (n == 1? "" : "s"); },
        formatSelectionTooBig: function (limit) { return "You can only select " + limit + " item" + (limit == 1 ? "" : "s"); },
        formatLoadMore: function (pageNumber) { return "Loading more results…"; },
        formatSearching: function () { return "Searching…"; },
        minimumResultsForSearch: 0,
        minimumInputLength: 0,
        maximumInputLength: null,
        maximumSelectionSize: 0,
        id: function (e) { return e == undefined ? null : e.id; },
        matcher: function(term, text) {
            return stripDiacritics(''+text).toUpperCase().indexOf(stripDiacritics(''+term).toUpperCase()) >= 0;
        },
        separator: ",",
        tokenSeparators: [],
        tokenizer: defaultTokenizer,
        escapeMarkup: defaultEscapeMarkup,
        blurOnChange: false,
        selectOnBlur: false,
        adaptContainerCssClass: function(c) { return c; },
        adaptDropdownCssClass: function(c) { return null; },
        nextSearchTerm: function(selectedObject, currentSearchTerm) { return undefined; },
        searchInputPlaceholder: '',
        createSearchChoicePosition: 'top',
        shouldFocusInput: function (instance) {
            // Attempt to detect touch devices
            var supportsTouchEvents = (('ontouchstart' in window) ||
                                       (navigator.msMaxTouchPoints > 0));

            // Only devices which support touch events should be special cased
            if (!supportsTouchEvents) {
                return true;
            }

            // Never focus the input if search is disabled
            if (instance.opts.minimumResultsForSearch < 0) {
                return false;
            }

            return true;
        }
    };

    $.fn.select2.ajaxDefaults = {
        transport: $.ajax,
        params: {
            type: "GET",
            cache: false,
            dataType: "json"
        }
    };

    // exports
    window.Select2 = {
        query: {
            ajax: ajax,
            local: local,
            tags: tags
        }, util: {
            debounce: debounce,
            markMatch: markMatch,
            escapeMarkup: defaultEscapeMarkup,
            stripDiacritics: stripDiacritics
        }, "class": {
            "abstract": AbstractSelect2,
            "single": SingleSelect2,
            "multi": MultiSelect2
        }
    };

}(jQuery));

/**
 * Enhanced Select2 Dropmenus
 *
 * @AJAX Mode - When in this mode, your value will be an object (or array of objects) of the data used by Select2
 *     This change is so that you do not have to do an additional query yourself on top of Select2's own query
 * @params [options] {object} The configuration options passed to $.fn.select2(). Refer to the documentation
 */
angular.module('ui.select2', []).value('uiSelect2Config', {}).directive('uiSelect2', ['uiSelect2Config', '$timeout', function (uiSelect2Config, $timeout) {
  var options = {};
  if (uiSelect2Config) {
    angular.extend(options, uiSelect2Config);
  }
  return {
    require: 'ngModel',
    priority: 1,
    compile: function (tElm, tAttrs) {
      var watch,
        repeatOption,
        repeatAttr,
        isSelect = tElm.is('select'),
        isMultiple = angular.isDefined(tAttrs.multiple);

      // Enable watching of the options dataset if in use
      if (tElm.is('select')) {
        repeatOption = tElm.find('option[ng-repeat], option[data-ng-repeat]');

        if (repeatOption.length) {
          repeatAttr = repeatOption.attr('ng-repeat') || repeatOption.attr('data-ng-repeat');
          watch = jQuery.trim(repeatAttr.split('|')[0]).split(' ').pop();
        }
      }

      return function (scope, elm, attrs, controller) {
        // instance-specific options
        var opts = angular.extend({}, options, scope.$eval(attrs.uiSelect2));

        /*
        Convert from Select2 view-model to Angular view-model.
        */
        var convertToAngularModel = function(select2_data) {
          var model;
          if (opts.simple_tags) {
            model = [];
            angular.forEach(select2_data, function(value, index) {
              model.push(value.id);
            });
          } else {
            model = select2_data;
          }
          return model;
        };

        /*
        Convert from Angular view-model to Select2 view-model.
        */
        var convertToSelect2Model = function(angular_data) {
          var model = [];
          if (!angular_data) {
            return model;
          }

          if (opts.simple_tags) {
            model = [];
            angular.forEach(
              angular_data,
              function(value, index) {
                model.push({'id': value, 'text': value});
              });
          } else {
            model = angular_data;
          }
          return model;
        };

        if (isSelect) {
          // Use <select multiple> instead
          delete opts.multiple;
          delete opts.initSelection;
        } else if (isMultiple) {
          opts.multiple = true;
        }

        if (controller) {
          // Watch the model for programmatic changes
           scope.$watch(tAttrs.ngModel, function(current, old) {
            if (!current) {
              return;
            }
            if (current === old) {
              return;
            }
            controller.$render();
          }, true);
          controller.$render = function () {
            if (isSelect) {
              elm.select2('val', controller.$viewValue);
            } else {
              if (opts.multiple) {
                var viewValue = controller.$viewValue;
                if (angular.isString(viewValue)) {
                  viewValue = viewValue.split(',');
                }
                elm.select2(
                  'data', convertToSelect2Model(viewValue));
              } else {
                if (angular.isObject(controller.$viewValue)) {
                  elm.select2('data', controller.$viewValue);
                } else if (!controller.$viewValue) {
                  elm.select2('data', null);
                } else {
                  elm.select2('val', controller.$viewValue);
                }
              }
            }
          };

          // Watch the options dataset for changes
          if (watch) {
            scope.$watch(watch, function (newVal, oldVal, scope) {
              if (angular.equals(newVal, oldVal)) {
                return;
              }
              // Delayed so that the options have time to be rendered
              $timeout(function () {
                elm.select2('val', controller.$viewValue);
                // Refresh angular to remove the superfluous option
                elm.trigger('change');
                if(newVal && !oldVal && controller.$setPristine) {
                  controller.$setPristine(true);
                }
              });
            });
          }

          // Update valid and dirty statuses
          controller.$parsers.push(function (value) {
            var div = elm.prev();
            div
              .toggleClass('ng-invalid', !controller.$valid)
              .toggleClass('ng-valid', controller.$valid)
              .toggleClass('ng-invalid-required', !controller.$valid)
              .toggleClass('ng-valid-required', controller.$valid)
              .toggleClass('ng-dirty', controller.$dirty)
              .toggleClass('ng-pristine', controller.$pristine);
            return value;
          });

          if (!isSelect) {
            // Set the view and model value and update the angular template manually for the ajax/multiple select2.
            elm.bind("change", function (e) {
              e.stopImmediatePropagation();
              
              if (scope.$$phase || scope.$root.$$phase) {
                return;
              }
              scope.$apply(function () {
                controller.$setViewValue(
                  convertToAngularModel(elm.select2('data')));
              });
            });

            if (opts.initSelection) {
              var initSelection = opts.initSelection;
              opts.initSelection = function (element, callback) {
                initSelection(element, function (value) {
                  controller.$setViewValue(convertToAngularModel(value));
                  callback(value);
                });
              };
            }
          }
        }

        elm.bind("$destroy", function() {
          elm.select2("destroy");
        });

        attrs.$observe('disabled', function (value) {
          elm.select2('enable', !value);
        });

        attrs.$observe('readonly', function (value) {
          elm.select2('readonly', !!value);
        });

        if (attrs.ngMultiple) {
          scope.$watch(attrs.ngMultiple, function(newVal) {
            attrs.$set('multiple', !!newVal);
            elm.select2(opts);
          });
        }

        // Initialize the plugin late so that the injected DOM does not disrupt the template compiler
        $timeout(function () {
          elm.select2(opts);

          // Set initial value - I'm not sure about this but it seems to need to be there
          elm.val(controller.$viewValue);
          // important!
          controller.$render();

          // Not sure if I should just check for !isSelect OR if I should check for 'tags' key
          if (!opts.initSelection && !isSelect) {
            controller.$setViewValue(
              convertToAngularModel(elm.select2('data'))
            );
          }
        });
      };
    }
  };
}]);

/*! 2.0.5 */
!function(){function a(a,b){window.XMLHttpRequest.prototype[a]=b(window.XMLHttpRequest.prototype[a])}function b(a,b,c,d,e,f){function g(a,b,c,d,e){f(function(){for(var b=[],g=0;g<a.length;g++)b.push(a.item(g));c&&(d.fileModel=b,c&&c.$setViewValue(null!=b&&0==b.length?"":b)),f(function(){d.change({$files:b,$event:e})})})}a.multiple()&&(b.attr("multiple","true"),c.multiple="true");var h=a.accept();if(h&&(b.attr("accept",h),c.accept=h),"input"!==b[0].tagName.toLowerCase()||"file"!==(b.attr("type")&&b.attr("type").toLowerCase())){var i=angular.element('<input type="file">');c.multiple&&i.attr("multiple",c.multiple),c.accept&&i.attr("accept",c.accept),i.css("width","1px").css("height","1px").css("opacity",0).css("position","absolute").css("filter","alpha(opacity=0)").css("padding",0).css("margin",0).css("overflow","hidden").attr("tabindex","-1").attr("ng-file-generated-elem",!0),b.append(i),b.__afu_fileClickDelegate__=function(){i[0].click()},b.bind("click",b.__afu_fileClickDelegate__),b.css("overflow","hidden"),b=i}0!=a.resetOnClick()&&b.bind("click",function(e){b[0].value&&g([],c,d,a,e),b[0].value=null}),d&&a.$parent.$watch(c.ngModel,function(a){null==a&&(b[0].value=null)}),""!=c.ngFileSelect&&(a.change=a.select),b.bind("change",function(b){var e;e=b.__files_||b.target.files,g(e,c,d,a,b)})}function c(a,b,c,g,h,i,j){function k(a,b,c){var d=!0;if(r){var e=c.dataTransfer.items;if(null!=e)for(var f=0;f<e.length&&d;f++)d=d&&("file"==e[f].kind||""==e[f].kind)&&(null!=e[f].type.match(r)||null!=e[f].name&&null!=e[f].name.match(r))}var g=a.dragOverClass({$event:c});return g&&(g.delay&&(p=g.delay),g.accept&&(g=d?g.accept:g.reject)),g||b.dragOverClass||"dragover"}function l(a,b,c,d){function f(a){!r||a.type.match(r)||null!=a.name&&a.name.match(r)?h.push(a):k.push(a)}function g(a,b,c){if(null!=b)if(b.isDirectory){f({name:b.name,type:"directory",path:(c?c:"")+b.name});var d=b.createReader();s++,d.readEntries(function(d){try{for(var e=0;e<d.length;e++)g(a,d[e],(c?c:"")+b.name+"/")}finally{s--}})}else s++,b.file(function(a){s--,a.path=(c?c:"")+a.name,f(a)})}var h=[],k=[],l=a.dataTransfer.items;if(l&&l.length>0&&"file"!=j.protocol())for(var m=0;m<l.length;m++){if(l[m].webkitGetAsEntry&&l[m].webkitGetAsEntry()&&l[m].webkitGetAsEntry().isDirectory){var n=l[m].webkitGetAsEntry();if(n.isDirectory&&!c)continue;null!=n&&(e(n.name)?g(h,n):l[m].webkitGetAsEntry().isDirectory||f(l[m].getAsFile()))}else{var o=l[m].getAsFile();null!=o&&f(o)}if(!d&&h.length>0)break}else{var p=a.dataTransfer.files;if(null!=p)for(var m=0;m<p.length&&(f(p.item(m)),d||!(h.length>0));m++);}var q=0;!function t(a){i(function(){if(s)10*q++<2e4&&t(10);else{if(!d&&h.length>1){for(var a=0;"directory"==h[a].type;)a++;h=[h[a]]}b(h,k)}},a||0)}();var s=0}var m=d();if(c.dropAvailable&&i(function(){a.dropAvailable=m}),!m)return 0!=a.hideOnDropNotAvailable()&&b.css("display","none"),void 0;var n=null,o=a.stopPropagation(),p=1,q=a.accept()||c.accept||c.ngAccept,r=q?new RegExp(f(q)):null;b[0].addEventListener("dragover",function(d){d.preventDefault(),o&&d.stopPropagation(),i.cancel(n),a.actualDragOverClass||(a.actualDragOverClass=k(a,c,d)),b.addClass(a.actualDragOverClass)},!1),b[0].addEventListener("dragenter",function(a){a.preventDefault(),o&&a.stopPropagation()},!1),b[0].addEventListener("dragleave",function(){n=i(function(){b.removeClass(a.actualDragOverClass),a.actualDragOverClass=null},p||1)},!1),""!=c.ngFileDrop&&(a.change=a.drop),b[0].addEventListener("drop",function(d){d.preventDefault(),o&&d.stopPropagation(),b.removeClass(a.actualDragOverClass),a.actualDragOverClass=null,l(d,function(b,e){g&&(a.fileModel=b,g&&g.$setViewValue(null!=b&&0==b.length?"":b)),c.ngFileRejectedModel&&(a.fileRejectedModel=e),i(function(){a.change({$files:b,$rejectedFiles:e,$event:d})})},0!=a.allowDir(),c.multiple||a.multiple()||"true"==c.ngMultiple)},!1)}function d(){var a=document.createElement("div");return"draggable"in a&&"ondrop"in a}function e(a){return/^[\000-\177]*$/.test(a)}function f(a){if(a.length>2&&"/"===a[0]&&"/"===a[a.length-1])return a.substring(1,a.length-1);var b=a.split(","),c="";if(b.length>1)for(var d=0;d<b.length;d++)c+="("+f(b[d])+")",d<b.length-1&&(c+="|");else c="^"+a.replace(new RegExp("[.\\\\+*?\\[\\^\\]$(){}=!<>|:\\-]","g"),"\\$&")+"$",c=c.replace(/\\\*/g,".*").replace(/\\\?/g,".");return c}window.XMLHttpRequest&&!window.XMLHttpRequest.__isFileAPIShim&&a("setRequestHeader",function(a){return function(b,c){if("__setXHR_"===b){var d=c(this);d instanceof Function&&d(this)}else a.apply(this,arguments)}});var g=angular.module("angularFileUpload",[]);g.version="2.0.5",g.service("$upload",["$http","$q","$timeout",function(a,b,c){function d(d){d.method=d.method||"POST",d.headers=d.headers||{},d.transformRequest=d.transformRequest||function(b,c){return window.ArrayBuffer&&b instanceof window.ArrayBuffer?b:a.defaults.transformRequest[0](b,c)};var e=b.defer(),f=e.promise;return d.headers.__setXHR_=function(){return function(a){a&&(d.__XHR=a,d.xhrFn&&d.xhrFn(a),a.upload.addEventListener("progress",function(a){a.config=d,e.notify?e.notify(a):f.progress_fn&&c(function(){f.progress_fn(a)})},!1),a.upload.addEventListener("load",function(a){a.lengthComputable&&(a.config=d,e.notify?e.notify(a):f.progress_fn&&c(function(){f.progress_fn(a)}))},!1))}},a(d).then(function(a){e.resolve(a)},function(a){e.reject(a)},function(a){e.notify(a)}),f.success=function(a){return f.then(function(b){a(b.data,b.status,b.headers,d)}),f},f.error=function(a){return f.then(null,function(b){a(b.data,b.status,b.headers,d)}),f},f.progress=function(a){return f.progress_fn=a,f.then(null,null,function(b){a(b)}),f},f.abort=function(){return d.__XHR&&c(function(){d.__XHR.abort()}),f},f.xhr=function(a){return d.xhrFn=function(b){return function(){b&&b.apply(f,arguments),a.apply(f,arguments)}}(d.xhrFn),f},f}this.upload=function(b){b.headers=b.headers||{},b.headers["Content-Type"]=void 0,b.transformRequest=b.transformRequest||a.defaults.transformRequest;var c=new FormData,e=b.transformRequest,f=b.data;return b.transformRequest=function(a,c){if(f)if(b.formDataAppender)for(var d in f){var g=f[d];b.formDataAppender(a,d,g)}else for(var d in f){var g=f[d];if("function"==typeof e)g=e(g,c);else for(var h=0;h<e.length;h++){var i=e[h];"function"==typeof i&&(g=i(g,c))}void 0!=g&&a.append(d,g)}if(null!=b.file){var j=b.fileFormDataName||"file";if("[object Array]"===Object.prototype.toString.call(b.file))for(var k="[object String]"===Object.prototype.toString.call(j),h=0;h<b.file.length;h++)a.append(k?j:j[h],b.file[h],b.fileName&&b.fileName[h]||b.file[h].name);else a.append(j,b.file,b.fileName||b.file.name)}return a},b.data=c,d(b)},this.http=function(a){return d(a)}}]),g.directive("ngFileSelect",["$parse","$timeout",function(a,c){return{restrict:"AEC",require:"?ngModel",scope:{fileModel:"=ngModel",change:"&ngFileChange",select:"&ngFileSelect",resetOnClick:"&resetOnClick",multiple:"&ngMultiple",accept:"&ngAccept"},link:function(d,e,f,g){b(d,e,f,g,a,c)}}}]),g.directive("ngFileDrop",["$parse","$timeout","$location",function(a,b,d){return{restrict:"AEC",require:"?ngModel",scope:{fileModel:"=ngModel",fileRejectedModel:"=ngFileRejectedModel",change:"&ngFileChange",drop:"&ngFileDrop",allowDir:"&allowDir",dragOverClass:"&dragOverClass",dropAvailable:"=dropAvailable",stopPropagation:"&stopPropagation",hideOnDropNotAvailable:"&hideOnDropNotAvailable",multiple:"&ngMultiple",accept:"&ngAccept"},link:function(e,f,g,h){c(e,f,g,h,a,b,d)}}}]),g.directive("ngNoFileDrop",function(){return function(a,b){d()&&b.css("display","none")}}),g.directive("ngFileDropAvailable",["$parse","$timeout",function(a,b){return function(c,e,f){if(d()){var g=a(f.ngFileDropAvailable);b(function(){g(c)})}}}])}();
/*! 2.0.5 */
!function(){function a(a,b){window.XMLHttpRequest.prototype[a]=b(window.XMLHttpRequest.prototype[a])}function b(a,b,c,d,e,f){function g(a,b,c,d,e){f(function(){for(var b=[],g=0;g<a.length;g++)b.push(a.item(g));c&&(d.fileModel=b,c&&c.$setViewValue(null!=b&&0==b.length?"":b)),f(function(){d.change({$files:b,$event:e})})})}a.multiple()&&(b.attr("multiple","true"),c.multiple="true");var h=a.accept();if(h&&(b.attr("accept",h),c.accept=h),"input"!==b[0].tagName.toLowerCase()||"file"!==(b.attr("type")&&b.attr("type").toLowerCase())){var i=angular.element('<input type="file">');c.multiple&&i.attr("multiple",c.multiple),c.accept&&i.attr("accept",c.accept),i.css("width","1px").css("height","1px").css("opacity",0).css("position","absolute").css("filter","alpha(opacity=0)").css("padding",0).css("margin",0).css("overflow","hidden").attr("tabindex","-1").attr("ng-file-generated-elem",!0),b.append(i),b.__afu_fileClickDelegate__=function(){i[0].click()},b.bind("click",b.__afu_fileClickDelegate__),b.css("overflow","hidden"),b=i}0!=a.resetOnClick()&&b.bind("click",function(e){b[0].value&&g([],c,d,a,e),b[0].value=null}),d&&a.$parent.$watch(c.ngModel,function(a){null==a&&(b[0].value=null)}),""!=c.ngFileSelect&&(a.change=a.select),b.bind("change",function(b){var e;e=b.__files_||b.target.files,g(e,c,d,a,b)})}function c(a,b,c,g,h,i,j){function k(a,b,c){var d=!0;if(r){var e=c.dataTransfer.items;if(null!=e)for(var f=0;f<e.length&&d;f++)d=d&&("file"==e[f].kind||""==e[f].kind)&&(null!=e[f].type.match(r)||null!=e[f].name&&null!=e[f].name.match(r))}var g=a.dragOverClass({$event:c});return g&&(g.delay&&(p=g.delay),g.accept&&(g=d?g.accept:g.reject)),g||b.dragOverClass||"dragover"}function l(a,b,c,d){function f(a){!r||a.type.match(r)||null!=a.name&&a.name.match(r)?h.push(a):k.push(a)}function g(a,b,c){if(null!=b)if(b.isDirectory){f({name:b.name,type:"directory",path:(c?c:"")+b.name});var d=b.createReader();s++,d.readEntries(function(d){try{for(var e=0;e<d.length;e++)g(a,d[e],(c?c:"")+b.name+"/")}finally{s--}})}else s++,b.file(function(a){s--,a.path=(c?c:"")+a.name,f(a)})}var h=[],k=[],l=a.dataTransfer.items;if(l&&l.length>0&&"file"!=j.protocol())for(var m=0;m<l.length;m++){if(l[m].webkitGetAsEntry&&l[m].webkitGetAsEntry()&&l[m].webkitGetAsEntry().isDirectory){var n=l[m].webkitGetAsEntry();if(n.isDirectory&&!c)continue;null!=n&&(e(n.name)?g(h,n):l[m].webkitGetAsEntry().isDirectory||f(l[m].getAsFile()))}else{var o=l[m].getAsFile();null!=o&&f(o)}if(!d&&h.length>0)break}else{var p=a.dataTransfer.files;if(null!=p)for(var m=0;m<p.length&&(f(p.item(m)),d||!(h.length>0));m++);}var q=0;!function t(a){i(function(){if(s)10*q++<2e4&&t(10);else{if(!d&&h.length>1){for(var a=0;"directory"==h[a].type;)a++;h=[h[a]]}b(h,k)}},a||0)}();var s=0}var m=d();if(c.dropAvailable&&i(function(){a.dropAvailable=m}),!m)return 0!=a.hideOnDropNotAvailable()&&b.css("display","none"),void 0;var n=null,o=a.stopPropagation(),p=1,q=a.accept()||c.accept||c.ngAccept,r=q?new RegExp(f(q)):null;b[0].addEventListener("dragover",function(d){d.preventDefault(),o&&d.stopPropagation(),i.cancel(n),a.actualDragOverClass||(a.actualDragOverClass=k(a,c,d)),b.addClass(a.actualDragOverClass)},!1),b[0].addEventListener("dragenter",function(a){a.preventDefault(),o&&a.stopPropagation()},!1),b[0].addEventListener("dragleave",function(){n=i(function(){b.removeClass(a.actualDragOverClass),a.actualDragOverClass=null},p||1)},!1),""!=c.ngFileDrop&&(a.change=a.drop),b[0].addEventListener("drop",function(d){d.preventDefault(),o&&d.stopPropagation(),b.removeClass(a.actualDragOverClass),a.actualDragOverClass=null,l(d,function(b,e){g&&(a.fileModel=b,g&&g.$setViewValue(null!=b&&0==b.length?"":b)),c.ngFileRejectedModel&&(a.fileRejectedModel=e),i(function(){a.change({$files:b,$rejectedFiles:e,$event:d})})},0!=a.allowDir(),c.multiple||a.multiple()||"true"==c.ngMultiple)},!1)}function d(){var a=document.createElement("div");return"draggable"in a&&"ondrop"in a}function e(a){return/^[\000-\177]*$/.test(a)}function f(a){if(a.length>2&&"/"===a[0]&&"/"===a[a.length-1])return a.substring(1,a.length-1);var b=a.split(","),c="";if(b.length>1)for(var d=0;d<b.length;d++)c+="("+f(b[d])+")",d<b.length-1&&(c+="|");else c="^"+a.replace(new RegExp("[.\\\\+*?\\[\\^\\]$(){}=!<>|:\\-]","g"),"\\$&")+"$",c=c.replace(/\\\*/g,".*").replace(/\\\?/g,".");return c}window.XMLHttpRequest&&!window.XMLHttpRequest.__isFileAPIShim&&a("setRequestHeader",function(a){return function(b,c){if("__setXHR_"===b){var d=c(this);d instanceof Function&&d(this)}else a.apply(this,arguments)}});var g=angular.module("angularFileUpload",[]);g.version="2.0.5",g.service("$upload",["$http","$q","$timeout",function(a,b,c){function d(d){d.method=d.method||"POST",d.headers=d.headers||{},d.transformRequest=d.transformRequest||function(b,c){return window.ArrayBuffer&&b instanceof window.ArrayBuffer?b:a.defaults.transformRequest[0](b,c)};var e=b.defer(),f=e.promise;return d.headers.__setXHR_=function(){return function(a){a&&(d.__XHR=a,d.xhrFn&&d.xhrFn(a),a.upload.addEventListener("progress",function(a){a.config=d,e.notify?e.notify(a):f.progress_fn&&c(function(){f.progress_fn(a)})},!1),a.upload.addEventListener("load",function(a){a.lengthComputable&&(a.config=d,e.notify?e.notify(a):f.progress_fn&&c(function(){f.progress_fn(a)}))},!1))}},a(d).then(function(a){e.resolve(a)},function(a){e.reject(a)},function(a){e.notify(a)}),f.success=function(a){return f.then(function(b){a(b.data,b.status,b.headers,d)}),f},f.error=function(a){return f.then(null,function(b){a(b.data,b.status,b.headers,d)}),f},f.progress=function(a){return f.progress_fn=a,f.then(null,null,function(b){a(b)}),f},f.abort=function(){return d.__XHR&&c(function(){d.__XHR.abort()}),f},f.xhr=function(a){return d.xhrFn=function(b){return function(){b&&b.apply(f,arguments),a.apply(f,arguments)}}(d.xhrFn),f},f}this.upload=function(b){b.headers=b.headers||{},b.headers["Content-Type"]=void 0,b.transformRequest=b.transformRequest||a.defaults.transformRequest;var c=new FormData,e=b.transformRequest,f=b.data;return b.transformRequest=function(a,c){if(f)if(b.formDataAppender)for(var d in f){var g=f[d];b.formDataAppender(a,d,g)}else for(var d in f){var g=f[d];if("function"==typeof e)g=e(g,c);else for(var h=0;h<e.length;h++){var i=e[h];"function"==typeof i&&(g=i(g,c))}void 0!=g&&a.append(d,g)}if(null!=b.file){var j=b.fileFormDataName||"file";if("[object Array]"===Object.prototype.toString.call(b.file))for(var k="[object String]"===Object.prototype.toString.call(j),h=0;h<b.file.length;h++)a.append(k?j:j[h],b.file[h],b.fileName&&b.fileName[h]||b.file[h].name);else a.append(j,b.file,b.fileName||b.file.name)}return a},b.data=c,d(b)},this.http=function(a){return d(a)}}]),g.directive("ngFileSelect",["$parse","$timeout",function(a,c){return{restrict:"AEC",require:"?ngModel",scope:{fileModel:"=ngModel",change:"&ngFileChange",select:"&ngFileSelect",resetOnClick:"&resetOnClick",multiple:"&ngMultiple",accept:"&ngAccept"},link:function(d,e,f,g){b(d,e,f,g,a,c)}}}]),g.directive("ngFileDrop",["$parse","$timeout","$location",function(a,b,d){return{restrict:"AEC",require:"?ngModel",scope:{fileModel:"=ngModel",fileRejectedModel:"=ngFileRejectedModel",change:"&ngFileChange",drop:"&ngFileDrop",allowDir:"&allowDir",dragOverClass:"&dragOverClass",dropAvailable:"=dropAvailable",stopPropagation:"&stopPropagation",hideOnDropNotAvailable:"&hideOnDropNotAvailable",multiple:"&ngMultiple",accept:"&ngAccept"},link:function(e,f,g,h){c(e,f,g,h,a,b,d)}}}]),g.directive("ngNoFileDrop",function(){return function(a,b){d()&&b.css("display","none")}}),g.directive("ngFileDropAvailable",["$parse","$timeout",function(a,b){return function(c,e,f){if(d()){var g=a(f.ngFileDropAvailable);b(function(){g(c)})}}}])}();
'use strict';

angular.module('debounce', [])
  .service('debounce', ['$timeout', function ($timeout) {
    return function (func, wait, immediate, invokeApply) {
      var timeout, args, context, result;
      function debounce() {
        /* jshint validthis:true */
        context = this;
        args = arguments;
        var later = function () {
          timeout = null;
          if (!immediate) {
            result = func.apply(context, args);
          }
        };
        var callNow = immediate && !timeout;
        if (timeout) {
          $timeout.cancel(timeout);
        }
        timeout = $timeout(later, wait, invokeApply);
        if (callNow) {
          result = func.apply(context, args);
        }
        return result;
      }
      debounce.cancel = function () {
        $timeout.cancel(timeout);
        timeout = null;
      };
      return debounce;
    };
  }])
  .directive('debounce', ['debounce', '$parse', function (debounce, $parse) {
    return {
      require: 'ngModel',
      priority: 999,
      link: function ($scope, $element, $attrs, ngModelController) {
        var debounceDuration = $parse($attrs.debounce)($scope);
        var immediate = !!$parse($attrs.immediate)($scope);
        var debouncedValue, pass;
        var prevRender = ngModelController.$render.bind(ngModelController);
        var commitSoon = debounce(function (viewValue) {
          pass = true;
          ngModelController.$$lastCommittedViewValue = debouncedValue;
          ngModelController.$setViewValue(viewValue);
          pass = false;
        }, parseInt(debounceDuration, 10), immediate);
        ngModelController.$render = function () {
          prevRender();
          commitSoon.cancel();
          //we must be first parser for this to work properly,
          //so we have priority 999 so that we unshift into parsers last
          debouncedValue = this.$viewValue;
        };
        ngModelController.$parsers.unshift(function (value) {
          if (pass) {
            debouncedValue = value;
            return value;
          } else {
            commitSoon(ngModelController.$viewValue);
            return debouncedValue;
          }
        });
      }
    };
  }]);
