import './extension.js';
import './dom_extension.js';

import {Driver} from './driver.js';

//helpers
function increment_value(element) {
	element.textContent = parseInt(element.textContent) + 1;
}

const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
function random_character() {
	return characters.charAt(Math.floor(Math.random() * characters.length));
}

function random_string() {
	let text = '';
	for(let i = 0; i <= 10; i++) {
		text += random_character();
	}
	return text;
}

const keys = ['Escape', 'KeyZ', 'F5', 'PageDown'];
function random_key() {
	return keys[Math.floor(Math.random() * keys.length)];
}

const driver = new Driver();

//click
const click_me = document.getElementById('click_me');
click_me.addEventListener('click', increment_value.bind(undefined, document.getElementById('click_counter')));
document.getElementById('simulate_click').addEventListener('click', () => driver.click(click_me));

//double click
const double_click_me = document.getElementById('double_click_me');
double_click_me.addEventListener('dblclick', increment_value.bind(undefined, document.getElementById('double_click_counter')));
document.getElementById('simulate_double_click').addEventListener('click', () => driver.doubleClick(double_click_me));

//right click
const right_click_me = document.getElementById('right_click_me');
right_click_me.addEventListener('contextmenu', increment_value.bind(undefined, document.getElementById('right_click_counter')));
document.getElementById('simulate_right_click').addEventListener('click', () => driver.contextMenu(right_click_me));

//drag and drop
const drag_me = document.getElementById('drag_me');

drag_me.addEventListener('dragstart', function(event) {
	this.style.backgroundColor = 'red';
	event.dataTransfer.effectAllowed = 'linkMove';
	event.dataTransfer.setData('text/plain', 'Text data');
	event.dataTransfer.setData('application/vnd.custom', 'Custom data');
});

drag_me.addEventListener('dragend', function() {
	this.style.backgroundColor = '';
});

const dropzone = document.getElementById('dropzone');

function dragover(event) {
	event.preventDefault();
	this.style.backgroundColor = 'yellow';
}

function dragleave() {
	this.style.backgroundColor = '';
}

function drop(event) {
	if(event.dataTransfer.types.includes('application/vnd.custom')) {
		event.preventDefault();
		dragleave.call(this);
		increment_value(document.getElementById('drag_and_drop_counter'));
	}
}

dropzone.addEventListener('dragenter', dragover);
dropzone.addEventListener('dragover', dragover);
dropzone.addEventListener('dragleave', dragleave);
dropzone.addEventListener('drop', drop);

document.getElementById('simulate_drag_and_drop').addEventListener('click', () => driver.dragAndDrop(drag_me, dropzone));

//form
const submit_me = document.getElementById('submit_me');
submit_me.addEventListener('submit', function(event) {
	event.preventDefault();
	increment_value(document.getElementById('submit_counter'));
});
document.getElementById('simulate_submit').addEventListener('click', () => driver.submit(submit_me));

//fill
const fill_me = submit_me['fill_me'];
document.getElementById('simulate_fill').addEventListener('click', () => driver.type(fill_me, random_string()));

//check
const check_me = submit_me['check_me'];
document.getElementById('simulate_check').addEventListener('click', () => check_me.checked ? driver.uncheck(check_me) : driver.check(check_me));

//link
const send_me = document.getElementById('send_me');
window.addEventListener('hashchange', increment_value.bind(undefined, document.getElementById('send_counter')));
document.getElementById('simulate_send').addEventListener('click', () => driver.click(document.getElementById('send_me_elsewhere')));

//press
const pressed = document.getElementById('pressed');
document.addEventListener('keydown', event => {console.log(event); pressed.textContent = event.key;});
document.getElementById('simulate_press_letter').addEventListener('click', () => driver.press([random_character()]));
document.getElementById('simulate_press_key').addEventListener('click', () => driver.press([random_key()]));

driver.click(click_me);
driver.doubleClick(double_click_me);
driver.contextMenu(right_click_me);
driver.dragAndDrop(drag_me, dropzone);
driver.type(fill_me, 'I\'ve been filled');
driver.check(check_me);
driver.submit(submit_me);
driver.click(send_me);
driver.press(['a']);
