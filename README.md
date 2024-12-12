# audio
java audio
JavaFX's Media class provides basic audio functionallity; however, it leaves a lot to be desired. In my music-app project I found that I had to supplement JavaFX with an external library if I wanted to be able to read the metadata for some of the formats playable by JavaFX. I intend to rectify this.

To do: come up with better name than audio

## My Supported Formats
| Container | Can Play | Can Read Metadata |
| :- | :- | :- |
| Mp3 | No | Yes |
| Mp4 | No | Yes |
| M4A | No | Yes |
| FLAC | No | Yes |
| OGG| No | No |
| WAV | No | Yes |
| WMA | No | No |

## JavaFX Supported Formats
| Container | Can Play | Can Read Metadata |
| :- | :- | :- |
| Mp3 | Yes | Yes |
| Mp4 | Yes | No |
| M4A | Yes | No |
| FLAC | No | No |
| OGG| No | No |
| WAV | Yes | No |
| WMA | No | No |

# Resources
One problem I have run into during the course of this project has been finding sample files. Formats like MP3 are easy enough to come by, but I have never actually encountered an AIF, OGG, or AAC file in the wild before. 

Whether it is for reference to a format I've never encountered before, or to view the variations in formats like M4A/mp4 (where every other sample file has a unique header for some reason), I have found the following sites useful:
* Sample Files
  * https://filesampleshub.com/format/audio
  * https://samples.ffmpeg.org/
* implementation details
  * MP3/ID3
    * https://id3.org/
  * FLAC
    * https://xiph.org/flac/format.html
    * https://xiph.org/vorbis/doc/v-comment.html
  * WAV(E)
    * http://www.topherlee.com/software/pcm-tut-wavformat.html
    * https://www.recordingblogs.com/wiki/list-chunk-of-a-wave-file
    * https://www.aelius.com/njh/wavemetatools/doc/riffmci.pdf
  * M4A
    * https://www.cimarronsystems.com/wp-content/uploads/2017/04/Elements-of-the-H.264-VideoAAC-Audio-MP4-Movie-v2_0.pdf
