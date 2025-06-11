function log(success, message) {
	let text = success ? 'Success' : 'Fail';
	if(message) {
		text += ': ';
		text += message;
	}
	if(this.debug) {
		console.log(text);
	}
}

function check_exception(exception, exception_assert, message) {
	if(!exception_assert) {
		this.success(message || 'Code throws an exception');
	}
	//check exception matches criteria
	else {
		const check = exception_assert.call(undefined, exception);
		if(check === undefined) {
			this.fail(`${message}: Exception assert must return a boolean`);
		}
		else if(check) {
			this.success(message || 'Code throws an exception matching criteria');
		}
		else {
			this.fail(`${message}: Code does not throw the good exception: Actual [${exception}]`);
		}
	}
}

export class Assert {
	constructor(debug) {
		this.debug = debug || false;
	}
	/**
	 * @param {string} message - The message associated to the test
	 */
	success(message) {
		//log
		log.call(this, true, message);
	}
	/**
	 * @param {string} message - The message associated to the test
	 */
	fail(message) {
		//log
		log.call(this, false, message);
		throw new Error(message);
	}
	equal(actual, expected, message) {
		actual === expected ? this.success(message) : this.fail(`${message}: Actual [${actual}] - Expected [${expected}]`);
	}
	notEqual(actual, notExpected, message) {
		actual !== notExpected ? this.success(message) : this.fail(`${message}: Actual [${actual}] - Not expected [${notExpected}]`);
	}
	similar(actual, expected, message) {
		Object.equals(actual, expected) ? this.success(message) : this.fail(`${message}: Actual [${actual}] - Expected [${expected}]`);
	}
	notSimilar(actual, notExpected, message) {
		!Object.equals(actual, notExpected) ? this.success(message) : this.fail(`${message}: Actual [${actual}] - Not expected [${notExpected}]`);
	}
	defined(value, message) {
		this.notEqual(value, undefined, message);
	}
	undefined(value, message) {
		this.equal(value, undefined, message);
	}
	null(value, message) {
		this.equal(value, null, message);
	}
	notNull(value, message) {
		this.notEqual(value, null, message);
	}
	ok(assertion, message) {
		this.equal(assertion, true, message);
	}
	notOk(assertion, message) {
		this.equal(assertion, false, message);
	}
	doesThrow(block, exception_assert, message) {
		try {
			block.call();
			this.fail(message || 'Code does not throw an exception');
		}
		catch(exception) {
			check_exception.call(this, exception, exception_assert, message);
		}
	}
	doesNotThrow(block, message) {
		try {
			block.call();
			this.success(message || 'Code does not throw an exception');
		}
		catch(exception) {
			this.fail(`${message}: Code throws an exception: ${exception}`);
		}
	}
	async doesThrowAsync(block, exception_assert, message) {
		try {
			await block.call();
			this.fail(message || 'Code does not throw an exception');
		}
		catch(exception) {
			check_exception.call(this, exception, exception_assert, message);
		}
	}
	async doesNotThrowAsync(block, message) {
		try {
			await block.call();
			this.success(message || 'Code does not throw an exception');
		}
		catch(exception) {
			this.fail(`${message}: Code throws an exception: ${exception}`);
		}
	}
}
