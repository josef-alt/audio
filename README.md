# audio
java audio
JavaFX's Media class provides basic audio functionallity; however, it leaves a lot to be desired. In my music-app project I found that I had to supplement JavaFX with an external library if I wanted to be able to read the metadata for some of the formats playable by JavaFX. I intend to rectify this.

To do: come up with better name than audio

## My Supported Formats
| Container | Can Play | Can Read Metadata |
| :- | :- | :- |
| AAC | No | No |
| AIF(F) | No | No |
| Mp3 | No | Yes |
| Mp4 | No | No |
| M4A | No | No |
| FLAC | No | Yes |
| OGG| No | No |
| WAV | No | No |
| WMA | No | No |

## JavaFX Supported Formats
| Container | Can Play | Can Read Metadata |
| :- | :- | :- |
| AAC | Yes | No |
| AIF(F) | Yes | No |
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
* https://filesampleshub.com/format/audio
* https://samples.ffmpeg.org/
* https://xiph.org/flac/format.html
* https://xiph.org/vorbis/doc/v-comment.html
