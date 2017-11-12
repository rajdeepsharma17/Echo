package com.example.raj.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.raj.echo.CurrentSongHelper
import com.example.raj.echo.Databases.EchoDatabase
import com.example.raj.echo.R
import com.example.raj.echo.Songs
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.audioVisualization
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.currentPosition
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.currentSongHelper
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.endTimeNext
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.fab
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.favoriteContent
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.glView
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.loopImageButton
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.mediaPlayer
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.nextImageButton
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.playpauseImageButton
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.previousImageButton
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.seekbar
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.shuffleImageButton
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.songArtistView
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.songTitleView
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.startTimeNext
import com.example.raj.echo.fragments.SongPlayingFragment.Statified.updateSongs
import com.example.raj.echo.fragments.SongPlayingFragment.staticated.onSongComplete
import com.example.raj.echo.fragments.SongPlayingFragment.staticated.playNext
import com.example.raj.echo.fragments.SongPlayingFragment.staticated.processInformation
import com.example.raj.echo.fragments.SongPlayingFragment.staticated.updateTextView
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment() {
    object Statified{
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

        var fab:ImageButton?= null
        var favoriteContent: EchoDatabase?=null

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


    }


    object staticated{
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MT_PREFS_LOOP = "Loop feature"
        fun onSongComplete(){
            if(Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            }else{
                if(Statified.currentSongHelper?.isLoop as Boolean){
                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)

                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.currentPosition = currentPosition
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)


                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity,Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }else{
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if(Statified.favoriteContent?.checkifIdExist(Statified.currentSongHelper?.songId?.toInt() as Int)as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
            }
        }
        fun updateTextView(songtitle : String, songArtist: String){
            Statified.songTitleView?.setText(songtitle)
            Statified.songArtistView?.setText(songArtist)
        }
        fun processInformation(mediaPlayer: MediaPlayer){
            val finaltime = mediaPlayer.duration
            val starttime = mediaPlayer.currentPosition
            Statified.seekbar?.max = finaltime
            Statified.startTimeNext?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(starttime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(starttime?.toLong() as Long)-
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(starttime?.toLong()as Long))))
            Statified.endTimeNext?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finaltime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finaltime?.toLong() as Long)-
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finaltime?.toLong()as Long))))
            Statified.seekbar?.setProgress(starttime)
            Handler().postDelayed(Statified.updateSongs,1000)

        }
        fun playNext(check : String){
            if(check.equals("PlayNextNormal",ignoreCase = true)){
                Statified.currentPosition = Statified.currentPosition + 1
            }else{
                if(check.equals("PlayNextLikeNormalShuffle",ignoreCase = true)) {
                    var randomObject = Random()
                    var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                    Statified.currentPosition = randomPosition

                }
                if(Statified.currentPosition == Statified.fetchSongs?.size){
                    Statified.currentPosition= 0
                }
                Statified.currentSongHelper?.isLoop = false
                var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                Statified.currentSongHelper?.songPath =nextSong?.songData
                Statified.currentSongHelper?.songTitle =nextSong?.songTitle
                Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                Statified.currentSongHelper?.songId = nextSong?.songID as Long

                updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)


                Statified.mediaPlayer?.reset()
                try{
                    Statified.mediaPlayer?.setDataSource(Statified.myActivity,Uri.parse(Statified.currentSongHelper?.songPath))
                    Statified.mediaPlayer?.prepare()
                    Statified.mediaPlayer?.start()
                    processInformation(Statified.mediaPlayer as MediaPlayer)
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }
            if(Statified.favoriteContent?.checkifIdExist(Statified.currentSongHelper?.songId?.toInt() as Int)as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
            }
        }

    }




    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        Statified.seekbar=view?.findViewById(R.id.seekBar)
        Statified.startTimeNext=view?.findViewById(R.id.startTime)
        Statified.endTimeNext=view?.findViewById(R.id.endTime)
        Statified.playpauseImageButton=view?.findViewById(R.id.playPauseButton)
        Statified.previousImageButton=view?.findViewById(R.id.previousButton)
        Statified.nextImageButton=view?.findViewById(R.id.nextButton)
        Statified.loopImageButton=view?.findViewById(R.id.loopButton)
        Statified.songArtistView=view?.findViewById(R.id.songArtist)
        Statified.shuffleImageButton=view?.findViewById(R.id.shuffleButton)
        Statified.songTitleView=view?.findViewById(R.id.songTitle)
        Statified.glView=view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        Statified.fab?.alpha = 0.8f


        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
    }

    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        Statified.audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop =false
        Statified.currentSongHelper?.isShuffle = false
        Statified.currentSongHelper = CurrentSongHelper()
        var path: String?= null
        var _songTitle: String?=null
        var _songArtist: String?=null
        var _sondID: Long = 0
        try{
            path = arguments.getString("path")
            _songTitle = arguments.getString("songTitle")
            _songArtist = arguments.getString("songArtist")
            _sondID = arguments.getInt("songID").toLong()
            Statified.currentPosition = arguments.getInt("songPosition")
            Statified.fetchSongs = arguments.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath =path
            Statified.currentSongHelper?.songTitle =_songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId =_sondID

            updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
    }
        catch(e : Exception){
        e.printStackTrace()
        }

        var fromFavoriteBottomBar= arguments.get("FavBottomBar") as? String
        if(fromFavoriteBottomBar != null){
            Statified.mediaPlayer=FavoriteFragment.Statified.mediaPlayer
        }else{

        Statified.mediaPlayer = MediaPlayer()
        Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try{
            Statified.mediaPlayer?.setDataSource(myActivity,Uri.parse(path))
            Statified.mediaPlayer?.prepare()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        Statified.mediaPlayer?.start()
        processInformation(Statified.mediaPlayer as MediaPlayer)
        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            Statified.currentSongHelper?.isPlaying = false
            Statified.mediaPlayer?.pause()
        }else{
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            Statified.currentSongHelper?.isPlaying =true
            Statified.mediaPlayer?.start()

        }}
        Statified.mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }
        onClickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context,0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature",false)
        if(isShuffleAllowed as Boolean){
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop =false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature",false)
        if(isLoopAllowed as Boolean){
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop =true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }else{
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        if(Statified.favoriteContent?.checkifIdExist(Statified.currentSongHelper?.songId?.toInt() as Int)as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
        }
    }
    fun onClickHandler(){
        Statified.fab?.setOnClickListener({
            if(Statified.favoriteContent?.checkifIdExist(Statified.currentSongHelper?.songId?.toInt() as Int)as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
                Statified.favoriteContent?.deletefavorite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity,"Deleted from favorites",Toast.LENGTH_SHORT).show()
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
                Statified.favoriteContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt(),Statified.currentSongHelper?.songArtist,
                        Statified.currentSongHelper?.songTitle,Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity,"Added to favorites",Toast.LENGTH_SHORT).show()
            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorshuffle= Statified.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorloop= Statified.myActivity?.getSharedPreferences(staticated.MT_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()


            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorshuffle?.putBoolean("feature",false)
                editorshuffle?.apply()
            }else{
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorshuffle?.putBoolean("feature",true)
                editorshuffle?.apply()
                editorloop?.putBoolean("feature",false)
                editorloop?.apply()

            }
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
            }else {
                playNext("PlayNextNormal")
            }
        })

        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if(Statified.currentSongHelper?.isLoop as Boolean){
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()

        })
        Statified.loopImageButton?.setOnClickListener({
            var editorshuffle= Statified.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorloop= Statified.myActivity?.getSharedPreferences(staticated.MT_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()

            if(Statified.currentSongHelper?.isLoop as Boolean){
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                Statified.currentSongHelper?.isLoop = false
                editorshuffle?.putBoolean("feature",false)
                editorshuffle?.apply()
            }else{
                Statified.currentSongHelper?.isShuffle = false
                Statified.currentSongHelper?.isLoop = true
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                editorshuffle?.putBoolean("feature",false)
                editorshuffle?.apply()
                editorloop?.putBoolean("feature",true)
                editorloop?.apply()

            }
        })

        Statified.playpauseImageButton?.setOnClickListener({
            if(Statified.mediaPlayer?.isPlaying as Boolean){
                Statified.mediaPlayer?.pause()
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                Statified.mediaPlayer?.start()
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        Statified.loopImageButton?.setOnClickListener({
            if(Statified.currentSongHelper?.isLoop as Boolean){
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }else{
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }
        })

    }

    fun playPrevious(){
        Statified.currentPosition = Statified.currentPosition-1
        if(Statified.currentPosition==-1){
            Statified.currentPosition=0
        }
        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        val nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.currentPosition = currentPosition
        Statified.currentSongHelper?.songId = nextSong?.songID as Long

        updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)


        Statified.mediaPlayer?.reset()
        try{
            Statified.mediaPlayer?.setDataSource(activity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            processInformation(Statified.mediaPlayer as MediaPlayer)
        }catch (e: Exception){
            e.printStackTrace()
        }

        if(Statified.favoriteContent?.checkifIdExist(Statified.currentSongHelper?.songId?.toInt() as Int)as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
        }
    }



}// Required empty public constructor
