const Effects = {};

Effects.Sortable = function(container, callback, handle_selector, checker_function, filter_selector) {
	let element_moving;
	let element_handle;
	let element_dragged;

	function mousedown(event) {
		element_dragged = false;
		//stop event to prevent text selection and to allow nested sortables
		event.stop();
		//find element moving and keep an handle on it
		if(handle_selector) {
			element_moving = container.children.find(c => c.contains(this));
		}
		else {
			element_moving = this;
		}
		element_handle = event.clientY;
		//change element style
		element_moving.style.opacity = '0.8';
		element_moving.style.zIndex = '1000';
		//add listeners on document
		document.addEventListener('mousemove', mousemove);
		document.addEventListener('mouseup', mouseup);
	}

	function mousemove(event) {
		element_dragged = true;
		const current_y_position = element_moving.getBoundingClientRect().top;
		//element goes up
		let previous_element = element_moving;
		while(previous_element.previousElementSibling && current_y_position <= previous_element.previousElementSibling.getBoundingClientRect().top) {
			previous_element = previous_element.previousElementSibling;
		}
		if(previous_element !== element_moving) {
			if(!checker_function || checker_function.call(undefined, element_moving, previous_element, container.children.indexOf(previous_element))) {
				//var previous_element_top = previous_element.getBoundingClientRect().top;
				element_moving.parentNode.insertBefore(element_moving, previous_element);
				element_handle -= previous_element.offsetHeight; //previous_element_top - current_y_position + event.clientY;
				element_moving.style.transform = `translate(0,${event.clientY - element_handle}px)`;
				if(callback) {
					callback.call(container);
				}
				return;
			}
		}
		//element goes down
		let next_element = element_moving;
		while(next_element.nextElementSibling && current_y_position >= next_element.nextElementSibling.getBoundingClientRect().top) {
			next_element = next_element.nextElementSibling;
		}
		if(next_element !== element_moving) {
			if(!checker_function || checker_function.call(undefined, element_moving, next_element, container.children.indexOf(next_element))) {
				//var next_element_top = next_element.getBoundingClientRect().top;
				element_moving.parentNode.insertBefore(element_moving, next_element.nextElementSibling);
				element_handle += next_element.offsetHeight; //next_element_top - current_y_position + event.clientY;
				element_moving.style.transform = `translate(0,${event.clientY - element_handle}px)`;
				if(callback) {
					callback.call(container);
				}
				return;
			}
		}
		element_moving.style.transform = `translate(0,${event.clientY - element_handle}px)`;
	}

	function mouseup() {
		//reset element style
		element_moving.style.transform = '';
		element_moving.style.opacity = '1';
		element_moving.style.zIndex = '0';
		element_moving = undefined;
		element_handle = undefined;
		//remove listeners on document
		document.removeEventListener('mousemove', mousemove);
		document.removeEventListener('mouseup', mouseup);
	}

	//prevent click if a drag occurred
	function prevent_click(event) {
		if(element_dragged) {
			element_dragged = false;
			event.stop();
		}
	}

	//prevent native drag
	function prevent_drag(event) {
		event.preventDefault();
	}

	function filter_node(node) {
		return node.nodeType === Node.ELEMENT_NODE && (!filter_selector || node.matches(filter_selector));
	}

	function enable_sortables(nodes) {
		nodes.filter(filter_node).forEach(enable_sortable);
	}

	function enable_sortable(element) {
		const handle = handle_selector ? element.querySelector(handle_selector) : element;
		//start dragging on mouse down
		handle.removeEventListener('mousedown', mousedown);
		handle.addEventListener('mousedown', mousedown);
		//prevent other actions
		handle.removeEventListener('click', prevent_click);
		handle.addEventListener('click', prevent_click);
		handle.removeEventListener('dragstart', prevent_drag);
		handle.addEventListener('dragstart', prevent_drag);
	}

	function disable_sortables(nodes) {
		nodes.filter(filter_node).forEach(disable_sortable);
	}

	function disable_sortable(element) {
		const handle = handle_selector ? element.querySelector(handle_selector) : element;
		//start dragging on mouse down
		handle.removeEventListener('mousedown', mousedown);
		//prevent other actions
		handle.removeEventListener('click', prevent_click);
		handle.removeEventListener('dragstart', prevent_drag);
	}

	//observe container to remove sortable feature on deleted children and make new children sortable
	const observer = new MutationObserver(function(mutations) {
		mutations.forEach(function(mutation) {
			disable_sortables(mutation.removedNodes);
			enable_sortables(mutation.addedNodes);
		});
	});
	observer.observe(container, {childList: true});

	enable_sortables(container.children);
};

Effects.SCROLL_TIME_NOTCH = 20;

Effects.Scroll = function(element, to, time) {
	return new Promise(resolve => {
		const speed = (to - element.scrollLeft) / time;
		const notch = speed * Effects.SCROLL_TIME_NOTCH;
		const interval = setInterval(function() {
			if(Math.abs(element.scrollLeft - to) > Math.abs(notch)) {
				element.scrollLeft += notch;
			}
			else {
				clearInterval(interval);
				element.scrollLeft = to;
				resolve();
			}
		}, Effects.SCROLL_TIME_NOTCH);
	});
};

export {Effects};
