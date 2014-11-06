package edu.hitsz.nlp.lm.berkeleylm;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.StupidBackoffLm;
import edu.berkeley.nlp.lm.cache.ArrayEncodedCachingLmWrapper;
import edu.berkeley.nlp.lm.io.LmReaders;

public class GoogleReaderTest
{
	@Test
	public void testHash() {
		final ArrayEncodedNgramLanguageModel<String> lm = LmReaders.readLmFromGoogleNgramDir(new File("/home/tm/windows/asr/lm/berkeleylm/googledir").getPath(), false, false);
		checkScores(lm);
	}

	@Test
	public void testHashCached() {
		final ArrayEncodedNgramLanguageModel<String> lm = LmReaders.readLmFromGoogleNgramDir(FileUtils.getFile("/home/tm/windows/asr/lm/berkeleylm/googledir").getPath(), false, false);
		checkScores(ArrayEncodedCachingLmWrapper.wrapWithCacheNotThreadSafe(lm));
	}

	@Test
	public void testCompressed() {
		final ArrayEncodedNgramLanguageModel<String> lm = LmReaders.readLmFromGoogleNgramDir(FileUtils.getFile("/home/tm/windows/asr/lm/berkeleylm/googledir").getPath(), true, false);
		checkScores(lm);
	}

	/**
	 * @param lm
	 */
	private void checkScores(final ArrayEncodedNgramLanguageModel<String> lm) {
		Assert.assertEquals(lm.getLogProb(Arrays.asList("of", "xxx")), Math.log(1f / 12765289150L), 1e-3);
		Assert.assertEquals(lm.getLogProb(Arrays.asList("the", "(")), Math.log(40000f / 19401194714L), 1e-3);
		Assert.assertEquals(lm.getLogProb(Arrays.asList("of", "the", "(")), Math.log(50f / 854) + 0 * Math.log(0.4), 1e-3);
		Assert.assertEquals(lm.getLogProb(Arrays.asList("a", "the", "(")), Math.log(40000f / 19401194714L) + Math.log(0.4), 1e-3);
		Assert.assertEquals(lm.getLogProb(Arrays.asList("a", ")", "(")), Math.log(8912668768L * 1.0f / 408012035092L) + 2 * Math.log(0.4), 1e-3);
		Assert.assertEquals(lm.getLogProb(Arrays.asList("the", "of", "a")), Math.log(3668f / 12765289150L) + Math.log(0.4), 1e-3);
		Assert.assertEquals(lm.getLogProb(Arrays.asList("of", "the", "(")), Math.log(50f / 854), 1e-3);
	}
	
	
	public static void main(String[] args) {
		new GoogleReaderTest().testHash();
	}

}

