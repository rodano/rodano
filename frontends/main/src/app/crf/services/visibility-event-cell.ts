/**
 * this describes a visibility event in the CRF, following the execution of a visibility criterion
 */
export interface CellVisibilityEvent {
	//the cell is identified by its layout uid and its own id
	layoutUid: string;
	cellId: string;
	//the most important things if visibility state convoyed
	shown: boolean;
}
