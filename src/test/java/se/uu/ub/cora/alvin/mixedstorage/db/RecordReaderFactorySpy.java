package se.uu.ub.cora.alvin.mixedstorage.db;

import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class RecordReaderFactorySpy implements RecordReaderFactory {

	public boolean factorWasCalled = false;
	public RecordReaderSpy factored;
	public int noOfRecordsToReturn = 1;

	@Override
	public RecordReader factor() {
		factorWasCalled = true;
		factored = new RecordReaderSpy();
		factored.noOfRecordsToReturn = noOfRecordsToReturn;
		return factored;
	}

}
