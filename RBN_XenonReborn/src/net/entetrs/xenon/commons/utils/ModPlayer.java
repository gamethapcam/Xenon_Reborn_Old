package net.entetrs.xenon.commons.utils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.quippy.javamod.mixer.dsp.AudioProcessor;
import de.quippy.javamod.mixer.dsp.DspProcessorCallBack;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.ModContainer;
import de.quippy.javamod.multimedia.mod.ModMixer;
import de.quippy.javamod.system.Helpers;

/**
 * ModPlayer (music AMIGA / ATARI-ST). Refonte basée sur JavaMod (MOD, XM, S3M,
 * etc.)
 * 
 * @see <a href="http://www.javamod.de/">http://www.javamod.de/</a>
 * 
 * @author robin
 *
 */
public final class ModPlayer implements DspProcessorCallBack
{
	private static Log log = LogFactory.getLog(ModPlayer.class);
	private static ModPlayer instance = new ModPlayer();

	private ModMixer mixer;
	private String resourceName;
	private float leftLevel;
	private float rightLevel;

	public static ModPlayer getInstance()
	{
		return instance;
	}

	static
	{
		try
		{
			log.info("Init ModPLayer");
			Helpers.registerAllClasses();
			log.info("Init ModPLayer : classes registered");
			Properties props = new Properties();
			props.setProperty(ModContainer.PROPERTY_PLAYER_ISP, "3");
			props.setProperty(ModContainer.PROPERTY_PLAYER_STEREO, "2");
			props.setProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, "0");
			props.setProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, "0");
			props.setProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, "1");
			props.setProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, "1");
			props.setProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, "16");
			props.setProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, "48000");
			log.info("Init ModPLayer : configuring container ...");
			MultimediaContainerManager.configureContainer(props);
			log.info("Init ModPLayer : configuring container configured");
		}
		catch (ClassNotFoundException e)
		{
			if (log.isErrorEnabled())
			{
				log.error("Impossible d'instancier JavaMod", e);
			}
		}
	}

	private ModPlayer()
	{

	}

	public float getLeftLevel()
	{
		return leftLevel;
	}

	public float getRightLevel()
	{
		return rightLevel;
	}

	/**
	 * charge un module placé en ressources.
	 * 
	 * @param musicNameResource
	 */
	private void load(String musicNameResource)
	{
		try
		{
			resourceName = musicNameResource;
			URL modUrl = ModPlayer.class.getClassLoader().getResource(musicNameResource);
			if (log.isInfoEnabled()) log.info("Chargement de " + modUrl);
			MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(modUrl);
			mixer = (ModMixer) multimediaContainer.createNewMixer();
			if (log.isInfoEnabled()) log.info("Playing " + getMusicName());		
		}
		catch (UnsupportedAudioFileException e)
		{
			if (log.isErrorEnabled())
			{
				log.error("Impossible d'instancier JavaMod", e);
			}
		}
	}
	
	/**
	 * lance la lecture du module sous forme de Thread daemon, en boucle
	 */
	public void playLoop(String musicNameResource)
	{
		play(musicNameResource, true);
	}

	/**
	 * lance la lecture du module sous forme de Thread daemon.
	 */
	public synchronized void play(String musicNameResource, boolean loop)
	{
		if (mixer!=null && mixer.isPlaying())
		{
			log.info("Stopping Music ..");
			mixer.stopPlayback();
		}
		
		Thread t = new Thread(() -> {		
			boolean mustLoop = false;
			do
			{	
				this.load(musicNameResource);
				long mixLength = mixer.getLengthInMilliseconds();
				log.info("Starting playback");
				AudioProcessor audioProcessor = new AudioProcessor(1024, 60);
				audioProcessor.addListener(this);
				mixer.setAudioProcessor(audioProcessor);
				mixer.startPlayback();
				long position = mixer.getMillisecondPosition();
				log.info(String.format("loop : %b, mixLength : %d, mixPosition %d", loop, mixLength, position));
				mustLoop = (position >= mixLength && loop);
				if (mustLoop && log.isInfoEnabled())
				{
					log.info("Loop MOD !");
				}
				else
				{
					log.info("No Loop ");
				}
			} while (mustLoop);
		});	
		t.setDaemon(true);
		t.start();
	}

	/**
	 * arrête la lecture du module.
	 */
	public synchronized void stop()
	{
		if (mixer != null)
		{
			Thread t = new Thread(mixer::stopPlayback);
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * @return le nom du module courant chargé.
	 */
	public String getMusicName()
	{
		return (mixer == null) ? "NO MODULE" : resourceName;
	}

	@Override
	public void currentSampleChanged(float[] left, float[] right)
	{
		leftLevel = calculateLevel(left);
		rightLevel = calculateLevel(right);
	}

	public float calculateLevel(float[] samples)
	{
		float currentLevel = 0;
		if (samples != null)
		{
			for (float v : samples)
			{
				if (v < 0) v *= -1f;
				// if (v > currentLevel) currentLevel = v;
				currentLevel += v;
			}
		}
		return currentLevel;
	}

}
