window.addEventListener('load', () => {
	const toc = document.getElementById('table-of-contents');

	//clear existing content
	while(toc.firstChild) {
		toc.removeChild(toc.firstChild);
	}

	const title = document.createElement('h2');
	title.textContent = 'Table of contents';
	toc.appendChild(title);

	const list = document.createElement('ol');
	toc.appendChild(list);

	function generate_toc(container, sections, parent_indices = []) {
		sections.forEach((section, index) => {
			const heading = section.querySelector('h2, h3, h4, h5, h6');
			if(heading) {
				const indices = [...parent_indices, index + 1];
				//add numbering
				heading.textContent = `${indices.join('.')}. ${heading.textContent.trim()}`;
				const item = document.createElement('li');
				const link = document.createElement('a');
				link.href = `#${section.id}`;
				link.textContent = heading.textContent.trim();
				item.appendChild(link);
				container.appendChild(item);

				const nested_sections = section.querySelectorAll(':scope > section[id]');
				if(nested_sections.length > 0) {
					const nester_list = document.createElement('ol');
					generate_toc(nester_list, nested_sections, indices);
					item.appendChild(nester_list);
				}
			}
		});
	}

	generate_toc(list, document.querySelectorAll('article > section[id]'));
});
