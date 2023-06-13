# CanvasVideoTranscriber
Transcribes videos on canvas to make a document you can CTRL + F in to get an A on home exams

I was unable to find a way to get the video segment links from the direkt url for the Get request of the video. If you find a solution feel free to make a PR.

To download a video and get this to transcribe it you need to get the get request url. You can find this by going into inspect mode -> Network -> Reloading the page (f5) -> Clicking play on the video. Once there you will see something by the name seg-x-v1-at.ts. Click on that.
You from there want to copy the request URL all the way to the "seg-" (DO NOT INCLUDE THE LAST NUMBERS AND FILE TYPE). Here is an example of how it looks: https://prnt.sc/q2ThoBKOMAUl

Next you need to get an api key from open ai and add it to the config as well as let the program in the main function know where your FFMPEG is installed.

The program will generate a folder on desktop where everything will be put. When running the program you will get 3 files: 1 video file of the entire video/lecture. 1 mp3 audio file of the lecture and lastly one txt file that contains
the entire transcription.

If a bunch of people find this useful and it gets a few stars then i'll make a GUI to use this easier.
