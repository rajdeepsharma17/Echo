package com.example.raj.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.raj.echo.CurrentSongHelper
import com.example.raj.echo.R
import com.example.raj.echo.Songs
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment() {
    var myActivity: Activity?=null
    var mediaPlayer:MediaPlayer?=null
    var startTimeNext:TextView?=null
    var endTimeNext: TextView?=null
    var playpauseImageButton: ImageButton?=null
    var previousImageButton: ImageButton?=null
    var nextImageButton: ImageButton?=null
    var loopImageButton: ImageButton?=null
    var seekbar: SeekBar?=null
    var songArtistView: TextView?=null
    var shuffleImageButton: ImageButton?=null
    var songTitleView: TextView?=null

    var currentSongHelper: CurrentSongHelper?=null
    var currentPosition: Int= 0
    var fetchSongs: ArrayList<Songs>?=null

    var audioVisualization: AudioVisualization?=null
    var glView: GLAudioVisualizationView?=null

    object staticated{
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MT_PREFS_LOOP = "Loop feature"
    }


    var updateSongs = object : Runnable{
        override fun run() {
            var getCurrent = mediaPlayer?.currentPosition
            startTimeNext?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long)-
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()as Long))))
            Handler().postDelayed(this,1000)

        }

    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        seekbar=view?.findViewById(R.id.seekBar)
        startTimeNext=view?.findViewById(R.id.startTime)
        endTimeNext=view?.findViewById(R.id.endTime)
        playpauseImageButton=view?.findViewById(R.id.playPauseButton)
        previousImageButton=view?.findViewById(R.id.previousButton)
        nextImageButton=view?.findViewById(R.id.nextButton)
        loopImageButton=view?.findViewById(R.id.loopButton)
        songArtistView=view?.findViewById(R.id.songArtist)
        shuffleImageButton=view?.findViewById(R.id.shuffleButton)
        songTitleView=view?.findViewById(R.id.songTitle)
        glView=view?.findViewById(R.id.visualizer_view)


        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
    }

    override fun onPause() {
        audioVisualization?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop =false
        currentSongHelper?.isShuffle = false
         currentSongHelper = CurrentSongHelper()
        var path: String?= null
        var _songTitle: String?=null
        var _songArtist: String?=null
        var _sondID: Long = 0
        try{
            path = arguments.getString("path")
            _songTitle = arguments.getString("songTitle")
            _songArtist = arguments.getString("songArtist")
            _sondID = arguments.getInt("songID").toLong()
            currentPosition = arguments.getInt("songPosition")
            fetchSongs = arguments.getParcelableArrayList("songData")

            currentSongHelper?.songPath =path
            currentSongHelper?.songTitle =_songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songId =_sondID

            updateTextView(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)
    }
        catch(e : Exception){
        e.printStackTrace()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try{
            mediaPlayer?.setDataSource(myActivity,Uri.parse(path))
            mediaPlayer?.prepare()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        mediaPlayer?.start()
        processInformation(mediaPlayer as MediaPlayer)
        if(currentSongHelper?.isPlaying as Boolean){
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            currentSongHelper?.isPlaying = false
            mediaPlayer?.pause()
        }else{
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            currentSongHelper?.isPlaying =true
            mediaPlayer?.start()

        }
        mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }
        onClickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context,0)
        audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature",false)
        if(isShuffleAllowed as Boolean){
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop =false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            currentSongHelper?.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature",false)
        if(isLoopAllowed as Boolean){
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop =true
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }else{
            currentSongHelper?.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
    }
    fun onClickHandler(){
        shuffleImageButton?.setOnClickListener({
            var editorshuffle= myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorloop= myActivity?.getSharedPreferences(staticated.MT_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()


            if(currentSongHelper?.isShuffle as Boolean){
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isShuffle = false
                editorshuffle?.putBoolean("feature",false)
                editorshuffle?.apply()
            }else{
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorshuffle?.putBoolean("feature",true)
                editorshuffle?.apply()
                editorloop?.putBoolean("feature",false)
                editorloop?.apply()

            }
        })
        nextImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if(currentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
            }else {
                playNext("PlayNextNormal")
            }
        })

        previousImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if(currentSongHelper?.isLoop as Boolean){
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()

        })
        loopImageButton?.setOnClickListener({
            var editorshuffle= myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorloop= myActivity?.getSharedPreferences(staticated.MT_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()

            if(currentSongHelper?.isLoop as Boolean){
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                currentSongHelper?.isLoop = false
                editorshuffle?.putBoolean("feature",false)
                editorshuffle?.apply()
            }else{
                currentSongHelper?.isShuffle = false
                currentSongHelper?.isLoop = true
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                editorshuffle?.putBoolean("feature",false)
                editorshuffle?.apply()
                editorloop?.putBoolean("feature",true)
                editorloop?.apply()

            }
        })

        playpauseImageButton?.setOnClickListener({
            if(mediaPlayer?.isPlaying as Boolean){
                mediaPlayer?.pause()
                playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                mediaPlayer?.start()
                playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        loopImageButton?.setOnClickListener({
            if(currentSongHelper?.isLoop as Boolean){
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }else{
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }
        })

    }
    fun playNext(check : String){
        if(check.equals("PlayNextNormal",ignoreCase = true)){
            currentPosition = currentPosition + 1
        }else{
            if(check.equals("PlayNextLikeNormalShuffle",ignoreCase = true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPosition

            }
            if(currentPosition == fetchSongs?.size){
                currentPosition= 0
            }
                currentSongHelper?.isLoop = false
                var nextSong = fetchSongs?.get(currentPosition)
                currentSongHelper?.songPath =nextSong?.songData
                currentSongHelper?.songTitle =nextSong?.songTitle
                currentSongHelper?.currentPosition = currentPosition
                currentSongHelper?.songId = nextSong?.songID as Long

            updateTextView(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)


            mediaPlayer?.reset()
            try{
                mediaPlayer?.setDataSource(myActivity,Uri.parse(currentSongHelper?.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                processInformation(mediaPlayer as MediaPlayer)
            }
            catch (e: Exception){
                e.printStackTrace()
            }
            }
            }
    fun playPrevious(){
        currentPosition = currentPosition-1
        if(currentPosition==-1){
            currentPosition=0
        }
        if(currentSongHelper?.isPlaying as Boolean){
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper?.isLoop = false
        val nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.currentPosition = currentPosition
        currentSongHelper?.songId = nextSong?.songID as Long

        updateTextView(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)


        mediaPlayer?.reset()
        try{
            mediaPlayer?.setDataSource(activity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            processInformation(mediaPlayer as MediaPlayer)
        }catch (e: Exception){
            e.printStackTrace()
        }


    }
    fun onSongComplete(){
        if(currentSongHelper?.isShuffle as Boolean) {
            playNext("PlayNextLikeNormalShuffle")
            currentSongHelper?.isPlaying = true
        }else{
            if(currentSongHelper?.isLoop as Boolean){
                currentSongHelper?.isPlaying = true
                var nextSong = fetchSongs?.get(currentPosition)

                currentSongHelper?.songTitle = nextSong?.songTitle
                currentSongHelper?.songPath = nextSong?.songData
                currentSongHelper?.currentPosition = currentPosition
                currentSongHelper?.songId = nextSong?.songID as Long

                updateTextView(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)


                mediaPlayer?.reset()
                try {
                    mediaPlayer?.setDataSource(myActivity,Uri.parse(currentSongHelper?.songPath))
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                    processInformation(mediaPlayer as MediaPlayer)
                }catch (e: Exception){
                    e.printStackTrace()
                }

            }else{
                playNext("PlayNextNormal")
                currentSongHelper?.isPlaying = true
            }
        }
    }
    fun updateTextView(songtitle : String, songArtist: String){
        songTitleView?.setText(songtitle)
        songArtistView?.setText(songArtist)
    }
    fun processInformation(mediaPlayer: MediaPlayer){
        val finaltime = mediaPlayer.duration
        val starttime = mediaPlayer.currentPosition
        seekbar?.max = finaltime
        startTimeNext?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(starttime?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(starttime?.toLong() as Long)-
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(starttime?.toLong()as Long))))
        endTimeNext?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finaltime?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(finaltime?.toLong() as Long)-
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finaltime?.toLong()as Long))))
    seekbar?.setProgress(starttime)
        Handler().postDelayed(updateSongs,1000)

    }


}// Required empty public constructor
