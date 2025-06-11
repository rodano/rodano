class PartialDate {
	constructor(date) {
		const parts = !Array.isArray(date) ? date.split('.') : date;
		if(parts.length !== 3) {
			throw new Error('Partial date must have 3 parts');
		}
		if(parts.some(p => p === '')) {
			throw new Error('Partial date must have 3 not empty parts');
		}
		function to_value(value) {
			const number = parseInt(value);
			if(!isNaN(number)) {
				return number;
			}
			return undefined;
		}
		this.day = to_value(parts[0]);
		this.month = to_value(parts[1]);
		this.year = to_value(parts[2]);
		//check validity
		if(this.isYearUnknown() && !this.isMonthUnknown()) {
			throw new Error('Year cannot be unknown if month is known');
		}
		if(this.isMonthUnknown() && !this.isDayUnknown()) {
			throw new Error('Month cannot be unknown if day is known');
		}
	}
	isDayUnknown() {
		return !this.day;
	}
	isMonthUnknown() {
		return !this.month;
	}
	isYearUnknown() {
		return !this.year;
	}
	compareTo(date) {
		//try comparing on years
		if(this.isYearUnknown() || date.isYearUnknown()) {
			return 0;
		}
		if(this.year !== date.year) {
			return this.year - date.year;
		}
		//try comparing on months
		if(this.isMonthUnknown() || date.isMonthUnknown()) {
			return 0;
		}
		if(this.month !== date.month) {
			return this.month - date.month;
		}
		//try comparing on days
		if(this.isDayUnknown() || date.isDayUnknown()) {
			return 0;
		}
		return this.day - date.day;
	}
	isBefore(date) {
		return this.compareTo(date) < 0;
	}
	isAfter(date) {
		return date.isBefore(this);
	}
}

PartialDate.UNKNOWN = 'Unknown';

export {PartialDate};
