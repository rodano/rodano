/**
 * this describes a visibility event in the CRF, following the execution of a visibility criterion
 */
export interface LayoutVisibilityEvent {
	//the layout is identified by its id
	layoutId: string;
	//the most important things if visibility state convoyed
	shown: boolean;
}
