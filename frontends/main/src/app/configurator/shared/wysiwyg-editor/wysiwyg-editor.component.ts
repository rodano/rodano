import {Component, ElementRef, Input, Output, EventEmitter, AfterViewInit, OnDestroy, ViewChild, forwardRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import Quill from 'quill';

@Component({
	selector: 'app-wysiwyg-editor',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './wysiwyg-editor.component.html',
	styleUrls: ['./wysiwyg-editor.component.css'],
	providers: [
		{
			provide: NG_VALUE_ACCESSOR,
			useExisting: forwardRef(() => WysiwygEditorComponent),
			multi: true
		}
	]
})
export class WysiwygEditorComponent implements AfterViewInit, OnDestroy, ControlValueAccessor {
	@ViewChild('editorContainer') editorContainer!: ElementRef;
	@Input() placeholder = 'Enter text...';
	@Output() contentChange = new EventEmitter<string>();

	private quill!: Quill;
	private initialValue = '';
	private onChange: (value: string) => void = () => undefined;
	private onTouched: () => void = () => undefined;

	toolbarOptions = [
		['bold', 'italic', 'underline', 'strike'],
		['blockquote', 'code-block'],
		['link', 'formula'],

		[{list: 'ordered'}, {list: 'bullet'}],
		[{script: 'sub'}, {script: 'super'}],
		[{indent: '-1'}, {indent: '+1'}],
		[{direction: 'rtl'}],

		[{header: [1, 2, 3, 4, 5, 6, false]}],

		[{color: []}, {background: []}],
		[{align: []}],

		['clean']
	];

	ngAfterViewInit(): void {
		this.quill = new Quill(this.editorContainer.nativeElement, {
			modules: {
				toolbar: this.toolbarOptions
			},
			theme: 'snow',
			placeholder: this.placeholder
		});

		if(this.initialValue) {
			this.quill.clipboard.dangerouslyPasteHTML(this.initialValue);
		}

		this.quill.on('text-change', () => {
			const content = this.quill.getText().trim()
				? this.quill.getSemanticHTML()
				: '';
			this.onChange(content);
			this.contentChange.emit(content);
		});

		this.quill.root.addEventListener('blur', () => this.onTouched());
	}

	ngOnDestroy(): void {
		this.quill?.off('text-change');
	}

	writeValue(value: string): void {
		this.initialValue = value || '';
		if(this.quill) {
			this.quill.clipboard.dangerouslyPasteHTML(this.initialValue);
		}
	}

	registerOnChange(fn: (value: string) => void): void {
		this.onChange = fn;
	}

	registerOnTouched(fn: () => void): void {
		this.onTouched = fn;
	}
}
