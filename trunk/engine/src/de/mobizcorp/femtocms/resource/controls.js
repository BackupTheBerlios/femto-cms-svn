// femtocms minimalistic content management.
// Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.

// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

var controls;
var hiding;
var editor;
var over;

function init_controls() {
  attach_handler(document.body, 'onmouseover', hover_controls);
}

function attach_handler(tag,e,h) {
  if (tag && tag.nodeType == 1) {
    try {
      if (tag.getAttribute("femtocms:edit-id")) {
        tag[e] = h;
      }
    } catch (e) { }
    var scan = tag.firstChild;
    while (scan) {
      attach_handler(scan,e,h);
      scan = scan.nextSibling;
    }
  }
}

function create_controls() {
  controls = document.getElementById("controls");
}

function hover_controls(ev) {
  if (!ev) ev = window.event;
  var elem = ev.target ? ev.target : ev.srcElement;
  if (elem.nodeType == 3) elem = elem.parentElement; /*SAFARI*/
  if (editor || over == elem) return;
  if (!controls) create_controls();
  else keep_controls();
  var style = controls.style;
  style.top = delta(document.body, elem, 'offsetTop') + "px";
  style.left = delta(document.body, elem, 'offsetLeft') + "px";
  style.width = elem.offsetWidth + "px";
  style.height = elem.offsetHeight + "px";
  style.display = "";
  over = elem;
  return stop_event(ev);
}

function keep_controls() {
  if (hiding) {
    hiding = clearTimeout(hiding);
  }
}

function narrow_controls() {
  controls.style.display = "none";
}

function hide_controls() {
  if (controls && !hiding && !editor) {
    hiding = setTimeout("controls.style.display='none';", 500);
  }
}

function open_editor() {
  if (editor) return;
  var deco = document.getElementById("controls_deco")
  var w = deco.offsetWidth;
  if (w < 600) {
    w = 600;
  }
  if (w != deco.offsetWidth) {
    deco.style.width = w + "px";
  }
  deco.style.display = "";
  editor = new FCKeditor("fckeditor0");
  document.getElementById("edit-id").value = over.getAttribute("femtocms:edit-id");
  document.getElementById("edit-lastmodified").value = over.getAttribute("femtocms:edit-lastmodified");
  document.getElementById("fckeditor0").value = nodeToHTML(over);
  editor.BasePath = "/fckeditor/";
  editor.ReplaceTextarea();
}

function stop_event(ev) {
  if (ev.stopPropagation) {
    ev.stopPropagation();
  } else {
    ev.cancelBubble = true;
  }
  if (ev.preventDefault) {
    ev.preventDefault();
  } else if (ev.returnValue) {
    ev.returnValue = false;
  }
  return false;
}

function close_editor(ev) {
  document.location.replace(document.location.href);
  return stop_event(ev);
}

function nodeToHTML(tag,out,type) {
  if (!tag) return;
  var buf = out ? out : [];
  if (!type) {
    type = tag.nodeType;
  }
  if (type == 1) {
    // Element node.
    var open = false;
    buf.push('<', tag.nodeName);
    var atts = tag.attributes;
    var size = atts.length;
    for (var i = 0; i < size; i++) {
      nodeToHTML(atts[i], buf);
    }
    var scan = tag.firstChild;
    while (scan) {
      var type;
      try { type = scan.nodeType; } catch (e) { type = 2;/*IE50*/ }
      if (type != 2 && !open) {
        buf.push('>');
        open = true;
      }
      nodeToHTML(scan, buf);
      scan = scan.nextSibling;
    }
    if (open) {
      buf.push('</', tag.nodeName, '>');
    } else {
      buf.push('/>');
    }
  } else if (type == 2) {
    // Attribute node.
    var name = tag.nodeName;
    if (name.match(/^(?:on|femtocms:edit-)/i)) return;
    name = name.replace(/^femtocms:save-/, '');
    var value = tag.nodeValue ? String(tag.nodeValue) : "";
    if (value != "") {
      value = value.replace(/&/, '&amp;').replace(/"/, '&quot;');
      buf.push(' ', tag.nodeName, '="', value, '"');
    }
  } else if (type == 3) {
    // Text node.
    buf.push(tag.nodeValue);
  }
  if (!out) {
    return buf.join('');
  }
}

function delta(parent, child, name) {
  var result = 0;
  if (child) {
    result += child[name];
    if (parent != child) {
      result += delta(parent, child.offsetParent, name);
    }
  }
  return result;
}

window.setTimeout('init_controls()', 100);
