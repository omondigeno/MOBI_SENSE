package mmmobile.android;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import ca.uol.aig.fftpack.RealDoubleFFT;

import com.geno.ibmtask.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

public class Home extends MmActivity implements View.OnClickListener {

	static final int SAMPLE_DELAY = 200;
	static final float Y_AXIS_MAX_VALUE = 100f;
	int sampleRate = 8000;

	int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	int blockSize = 256;

	boolean started = false;
	AnalyseAudioTask analyseAudioTask;
	protected BarChart mChart;
	float xAxisIntervals = (sampleRate / 2) / blockSize;
	ArrayList<String> xVals;
	ArrayList<BarEntry> yVals1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState, R.layout.home, "");
			createDashboard();
		} catch (Exception e) {
			Log.d(Common.TAG, "activity error", e);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case R.id.start_stop_analysis:
			if (started) {

				started = false;

				((Button) arg0).setText("Start");
				analyseAudioTask.cancel(true);
			} else {
				analyseAudioTask = new AnalyseAudioTask();
				started = true;
				((Button) arg0).setText("Stop");
				analyseAudioTask.execute();
			}

			break;
		default:
			super.onClick(arg0);

		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void createDashboard() {

		Button startStopAnalysis = (Button) Home.this
				.findViewById(R.id.start_stop_analysis);
		startStopAnalysis.setOnClickListener(this);

		transformer = new RealDoubleFFT(blockSize);

		mChart = (BarChart) findViewById(R.id.chart1);

		// draw shadows for each bar that show the maximum value
		mChart.setDrawBarShadow(false);
		mChart.setDrawValueAboveBar(true);

		mChart.setDescription("");

		mChart.setMaxVisibleValueCount(blockSize);

		// scaling can now only be done on x- and y-axis separately
		mChart.setPinchZoom(false);

		mChart.setDrawGridBackground(false);

		XAxis xAxis = mChart.getXAxis();
		xAxis.setPosition(XAxisPosition.BOTTOM);
		xAxis.setDrawGridLines(false);

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
		leftAxis.setSpaceTop(15f);
		leftAxis.setAxisMaxValue(Y_AXIS_MAX_VALUE);
		
		YAxis rightAxis = mChart.getAxisRight();
		rightAxis.setDrawGridLines(false);
		rightAxis.setSpaceTop(15f);
		rightAxis.setAxisMaxValue(Y_AXIS_MAX_VALUE);

	}

	class AnalyseAudioTask extends AsyncTask<Void, double[], Void> {
		int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
				channelConfiguration, audioEncoding);

		AudioRecord audioRecord = new AudioRecord(

		MediaRecorder.AudioSource.MIC, sampleRate, channelConfiguration,
				audioEncoding, bufferSize);

		short[] buffer = new short[blockSize];
		double[] toTransform = new double[blockSize];

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(double[]... values) {
			super.onProgressUpdate(values);
			setData();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... arg0) {

			audioRecord.startRecording();

			while (started) {
				double sum = 0;
				int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
				for (int i = 0; i < blockSize && i < bufferReadResult; i++) {

					toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
					sum += buffer[i] * buffer[i];
				}
				transformer.ft(toTransform);
				prepareData(toTransform);
				publishProgress(toTransform);
				final double amplitude = sum / bufferReadResult;
				Log.d(Common.TAG,
						"sound in Db***************" + 20
								* Math.log10((int) Math.sqrt(amplitude)));

				try {
					Thread.sleep(SAMPLE_DELAY);
				} catch (InterruptedException e) {
					Log.d(Common.TAG, "InterruptedException", e);
				}
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			audioRecord.stop();
			audioRecord.release();
		}
	}

	private void prepareData(double[] toTransform) {

		xVals = new ArrayList<String>();
		for (int i = 0; i < toTransform.length; i++) {
			xVals.add(String.format("%.0fHz", (i * xAxisIntervals)));
		}

		yVals1 = new ArrayList<BarEntry>();

		for (int i = 0; i < toTransform.length; i++) {
			yVals1.add(new BarEntry((float) (toTransform[i]), i));
		}
	}

	private void setData() {

		BarDataSet set1 = new BarDataSet(yVals1, "Audio Pitch");
		set1.setBarSpacePercent(1f);

		ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
		dataSets.add(set1);

		BarData data = new BarData(xVals, dataSets);

		mChart.setData(data);
		mChart.invalidate();
	}
}