package ch.rodano.core.model.event;

public class Progression {
	private int progress;
	private int total;

	public Progression() {
		progress = 0;
		total = 0;
	}

	@Override
	public String toString() {
		return String.format("%d/%d", progress, total);
	}

	public float getPercentage() {
		return (float) progress / (float) total * 100f;
	}

	public int getPercentageRound() {
		return Math.round(getPercentage());
	}

	public void add(final Progression progression) {
		progress += progression.progress;
		total += progression.total;
	}

	public boolean isComplete() {
		return progress == total;
	}

	public boolean isNull() {
		return progress == 0;
	}

	public ProgressionCompletion getCompletion() {
		return isComplete() ? ProgressionCompletion.FULL_COMPLETE : isNull() ? ProgressionCompletion.NOT_COMPLETE : ProgressionCompletion.PARTIALLY_COMPLETE;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(final int progress) {
		this.progress = progress;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(final int total) {
		this.total = total;
	}
}
