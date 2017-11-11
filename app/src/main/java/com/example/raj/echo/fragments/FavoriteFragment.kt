package com.example.raj.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.raj.echo.R
import com.example.raj.echo.Songs
import com.example.raj.echo.adapters.FavoriteAdapter
import kotlinx.android.synthetic.main.fragment_favorite.*


/**
 * A simple [Fragment] subclass.
 */
class FavoriteFragment : Fragment() {

    var myActivity: Activity?=null
    var getSongList: ArrayList<Songs>?=null
    var noFavorites:TextView?=null
    var nowPlayingBottomBar:RelativeLayout?=null
    var playPauseButton:ImageButton?=null
    var songTitle:TextView?=null
    var recyclerView:RecyclerView?=null
    var trackPosition:Int=0
    object Statified{
        var mediaPlayer:MediaPlayer?=null
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       val view = inflater!!.inflate(R.layout.fragment_favorite, container, false)
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavMainScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        songTitle = view?.findViewById(R.id.songTitle)
        recyclerView = view?.findViewById(R.id.favoriteRecycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongList = getSongsFromPhone()
        if(getSongList==null){
            recyclerView?.visibility=View.INVISIBLE
            noFavorites?.visibility= View.VISIBLE
        }else{
            var favoriteAdapter=FavoriteAdapter(getSongList as ArrayList<Songs>,myActivity as Context)
            val mLayoutManager=LinearLayoutManager(activity)
            recyclerView?.layoutManager=mLayoutManager
            recyclerView?.itemAnimator= DefaultItemAnimator()
            recyclerView?.adapter= favoriteAdapter
            recyclerView?.setHasFixedSize(true)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
    }
    fun getSongsFromPhone(): ArrayList<Songs>{
        var arrayList  = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songURI,null,null,null,null)
        if(songCursor!=null && songCursor.moveToFirst()){
            val songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTiltle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while(songCursor.moveToNext()){
                var currentId = songCursor.getLong(songID)
                var currentTitle = songCursor.getString(songTiltle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist,currentData,currentDate))

            }
        }
        return arrayList

    }
    fun bottomBarSetup() = try {
        bottomBarClickHandler()
        songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
        SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.staticated.onSongComplete()
        })
        if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
            nowPlayingBottomBar?.visibility = View.VISIBLE
        }else{
            nowPlayingBottomBar?.visibility = View.INVISIBLE
        }

    }catch (e:Exception){
        e.printStackTrace()
    }
    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer= SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist",SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path",SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle",SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songID",SongPlayingFragment.Statified.currentSongHelper?.songId as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData",SongPlayingFragment.Statified.fetchSongs)
            songPlayingFragment.arguments = args

            fragmentManager.beginTransaction()
                    .replace(R.id.details_fragment,songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        })
        playPauseButton?.setOnClickListener({
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }

        })
    }

}// Required empty public constructor
