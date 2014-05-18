#CI Alert System Notes

##Sample
A sample alert system can be found in the test package **ceri.ci.sample** with resources under
test resource directory **ceri/ci/sample**

##Setting up Audio

###Audio files
There is no text-to-speech unfortunately, all audio comes from .wav files.
mp3 files are not currently supported.
50+ sound clips are included as alert announcements.
The following files are required for phrases:
- phrase/and.wav
- phrase/build.wav
- phrase/by.wav
- phrase/has_just_been_broken.wav
- phrase/is_now_fixed.wav
- phrase/is_still_broken.wav
- phrase/job.wav
- phrase/please_fix_it.wav
- phrase/thank_you.wav
- phrase/thanks_to.wav
- phrase/the_build.wav
Add the following files to add speech for build, job and name alerts:
- build/[build-name].wav for all build names
- job/[job-name].wav for all job names
- name/[name].wav for all names of committers
  
###Downloading speech files
Try these links to download text to speech files here:
http://www2.research.att.com/~ttsweb/tts/demo.php (free, limited voice types)
http://www.naturalreaders.com/index.php (more voices; inspect jquery_jplayer_1 div to find download content)
https://acapela-box.com/AcaBox/index.php (cost to download)
The sample files were generated from AT&T Natural Voices first link above. The voice is US English, Lauren.

###Conversion and Normalization
Audio files may need normalization to maintain for consistent volume with sound clips.
Audacity is a useful tool to convert mp3 to wav, and to normalize to -3.0db in batches.



