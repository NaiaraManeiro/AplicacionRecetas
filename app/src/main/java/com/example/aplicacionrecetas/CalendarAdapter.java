package com.example.aplicacionrecetas;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class CalendarAdapter extends FragmentPagerAdapter {

    private Context myContext;
    int totalTabs;

    public CalendarAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                MostrarEventosFragment mostrarFragment = new MostrarEventosFragment();
                return mostrarFragment;
            case 1:
                AnadirEventosFragment anadirFragment = new AnadirEventosFragment();
                return anadirFragment;
            case 2:
                EditarEventosFragment editarFragment = new EditarEventosFragment();
                return editarFragment;
            case 3:
                EliminarEventoFragment eliminarFragment = new EliminarEventoFragment();
                return eliminarFragment;
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}
