package com.piratemedia.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.content.ComponentName;

import com.android.music.IMediaPlaybackService;

public class updateService extends Service {
    
	public static final String MUSIC_CHANGED = "com.piratemedia.lockscreen.musicchanged";
	public static final String MUSIC_STOPPED = "com.piratemedia.lockscreen.musicstopped";
	public static final String SMS_CHANGED = "com.piratemedia.lockscreen.smschanged";
	public static final String PHONE_CHANGED = "com.piratemedia.lockscreen.phonechanged";
	public static final String MUTE_CHANGED = "com.piratemedia.lockscreen.mutechanged";
	public static final String WIFI_CHANGED = "com.piratemedia.lockscreen.wifichnaged";
	public IMediaPlaybackService mService = null;
	public static boolean playing = false;
	public String titleName;
	public String artistName;
	public String albumName;
	public long pos;
	public long dur;
	public long albumId;
	public long songId;
	public int batLevel;
	public int batpercentage;
	private boolean foreground=true;
	private NotificationManager mNM;

    @Override
    public void onCreate() {
        super.onCreate();
        //Start as foreground if user settings say so
    	if (utils.getCheckBoxPref(this, LockscreenSettings.SERVICE_FOREGROUND, true)) {
        	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        	CharSequence text = getText(R.string.service_notification);
            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.drawable.status_icon, text,
                    System.currentTimeMillis());
            // The PendingIntent to launch our activity if the user selects this notification
            Intent lockIntent=utils.getLockIntent(this);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,lockIntent, 0);
            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, getText(R.string.app_name),text, contentIntent);
            startForeground(R.string.airplane_mode, notification);
        }
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new intentReceiver();
        registerReceiver(mReceiver, filter);
    }

    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        
        onStart(aIntent, aStartId);

        return 2;
    }

    @Override
    public void onStart(Intent aIntent, int aStartId) {

        if (aIntent.getAction().equals("com.android.music.playbackcomplete") && getPlayer() == 1) {
            // The song has ended, stop the service
            stopSelf();
        } else if (aIntent.getAction().equals("com.android.music.playstatechanged") 
                || aIntent.getAction().equals("com.android.music.metachanged")
                || aIntent.getAction().equals("com.android.music.queuechanged")
                || aIntent.getAction().equals("com.android.music.playbackcomplete")
                && getPlayer() == 1) {

            bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
        
                public void onServiceConnected(ComponentName aName, IBinder aService) {
                	com.android.music.IMediaPlaybackService mService =
        				com.android.music.IMediaPlaybackService.Stub.asInterface(aService);
                	
                	try {
                		if (mService.isPlaying()){
                		
                		// Get info from service
                		playing = true;
                		titleName = mService.getTrackName();
                		artistName = mService.getArtistName();
                		albumName = mService.getAlbumName();
                		pos = mService.position();
                		dur = mService.duration();
                		albumId = mService.getAlbumId();
                		songId = mService.getAudioId();
                		
                		
                		if (mService.isPlaying()) {
                			notifyChange(MUSIC_CHANGED);
                			playing = true;
                		}
                        } else {
                        	notifyChange(MUSIC_STOPPED);
                        	playing = false;
                        }
                		
                	} catch (Exception e) {
                	e.printStackTrace();
                	throw new RuntimeException(e);
                	}

                    unbindService(this);
                }
                public void onServiceDisconnected(ComponentName aName) {
                    	playing = false;
                    	notifyChange(MUSIC_STOPPED);
                }

            }, 0);
        	} else if (aIntent.getAction().equals("com.htc.music.playbackcomplete") && getPlayer() == 2) {
                // The song has ended, stop the service
                stopSelf();
            } else if (aIntent.getAction().equals("com.htc.music.playstatechanged") 
                    || aIntent.getAction().equals("com.htc.music.metachanged")
                    || aIntent.getAction().equals("com.htc.music.queuechanged")
                    || aIntent.getAction().equals("com.htc.music.playbackcomplete")
                    && getPlayer() == 3) {

                bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
            
                    public void onServiceConnected(ComponentName aName, IBinder aService) {
                    	com.htc.music.IMediaPlaybackService mService =
            				com.htc.music.IMediaPlaybackService.Stub.asInterface(aService);
                    	
                    	try {
                    		if (mService.isPlaying()){
                    		
                    		// Get info from service
                    		playing = true;
                    		titleName = mService.getTrackName();
                    		artistName = mService.getArtistName();
                    		albumName = mService.getAlbumName();
                    		pos = mService.position();
                    		dur = mService.duration();
                    		albumId = mService.getAlbumId();
                    		songId = mService.getAudioId();
                    		
                    		
                    		if (mService.isPlaying()) {
                    			notifyChange(MUSIC_CHANGED);
                    			playing = true;
                    		}
                            } else {
                            	notifyChange(MUSIC_STOPPED);
                            	playing = false;
                            }
                    		
                    	} catch (Exception e) {
                    	e.printStackTrace();
                    	throw new RuntimeException(e);
                    	}

                        unbindService(this);
                    }
                    public void onServiceDisconnected(ComponentName aName) {
                        	playing = false;
                        	notifyChange(MUSIC_STOPPED);
                    }

                }, 0);
        	} else if (aIntent.getAction().equals("com.piratemedia.musicmod.playbackcomplete") && getPlayer() == 3) {
                // The song has ended, stop the service
                stopSelf();
            } else if (aIntent.getAction().equals("com.piratemedia.musicmod.playstatechanged") 
                    || aIntent.getAction().equals("com.piratemedia.musicmod.metachanged")
                    || aIntent.getAction().equals("com.piratemedia.musicmod.queuechanged")
                    || aIntent.getAction().equals("com.piratemedia.musicmod.playbackcomplete")
                    && getPlayer() == 3) {

                bindService(new Intent().setClassName("com.piratemedia.musicmod", "com.piratemedia.musicmod.MediaPlaybackService"), new ServiceConnection() {
            
                    public void onServiceConnected(ComponentName aName, IBinder aService) {
                    	com.piratemedia.musicmod.IMediaPlaybackService mService =
            				com.piratemedia.musicmod.IMediaPlaybackService.Stub.asInterface(aService);
                    	
                    	try {
                    		if (mService.isPlaying()){
                    		
                    		// Get info from service
                    		playing = true;
                    		titleName = mService.getTrackName();
                    		artistName = mService.getArtistName();
                    		albumName = mService.getAlbumName();
                    		pos = mService.position();
                    		dur = mService.duration();
                    		albumId = mService.getAlbumId();
                    		songId = mService.getAudioId();
                    		
                    		
                    		if (mService.isPlaying()) {
                    			notifyChange(MUSIC_CHANGED);
                    			playing = true;
                    		}
                            } else {
                            	notifyChange(MUSIC_STOPPED);
                            	playing = false;
                            }
                    		
                    	} catch (Exception e) {
                    	e.printStackTrace();
                    	throw new RuntimeException(e);
                    	}

                        unbindService(this);
                    }
                    public void onServiceDisconnected(ComponentName aName) {
                        	playing = false;
                        	notifyChange(MUSIC_STOPPED);
                    }

                }, 0);
            } else if (aIntent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            	notifyChange(SMS_CHANGED);
            } else if (aIntent.getAction().equals("android.intent.action.PHONE_STATE")) {
            	notifyChange(PHONE_CHANGED);
            } else if (aIntent.getAction().equals("android.media.RINGER_MODE_CHANGED")) {
            	notifyChange(MUTE_CHANGED);
            } else if (aIntent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
            	notifyChange(WIFI_CHANGED);
            } else if (aIntent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            	Log.d("Lockscreen", "Screen On");
            	if (!inCall()){
            		ManageKeyguard.disableKeyguard(getApplicationContext()); 

            	}
			} else if (aIntent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            	Log.d("Lockscreen", "Screen Off");
            	if (!inCall()){
            		Intent lock=utils.getLockIntent(this);
            		lock.setAction(utils.ACTION_LOCK);
            		startActivity(lock);
            		ManageKeyguard.reenableKeyguard(); 
            	}
            } else if(aIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            	Log.d("Lockscreen", "Boot Completed");
            	Intent lock=utils.getLockIntent(this);
            	lock.setAction(utils.ACTION_UNLOCK);
            	startActivity(lock);
            }
        
        }
    
private void notifyChange(String what) {
        
        Intent i = new Intent(what);
        i.putExtra("artist", artistName);
        i.putExtra("album", albumName);
        i.putExtra("track", titleName);
        i.putExtra("trackID", songId);
        i.putExtra("albumID", albumId);
        sendBroadcast(i);
    }
            public IBinder onBind(Intent aIntent) {

                return null;
            }
            
            // Set Which Media Player we want to use
        	public int getPlayer() {
        		String playerString = utils.getStringPref(this , LockscreenSettings.KEY_MUSIC_PLAYER, "1");
        		int player = Integer.parseInt(playerString);  
        		switch(player) {
        			case 1:
        				//Set Stock Music Player
        				return 1;
        			case 2:
        				//Set HTC Music as Player
        				return 2;
        			case 3:
        				//Set Music Mod as Player
        				return 3;
        		}
        		return 1;
        	}
		
		private boolean inCall() {
			TelephonyManager telMan =((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE));
	    	int state = telMan.getCallState();
	    	
	    	switch(state) {
	    		case TelephonyManager.CALL_STATE_IDLE:
	    			return false;
	    		case TelephonyManager.CALL_STATE_RINGING:
	    			return false;
	    		case TelephonyManager.CALL_STATE_OFFHOOK:
	    			return true;
	    	}
	    	return false;
		}
}