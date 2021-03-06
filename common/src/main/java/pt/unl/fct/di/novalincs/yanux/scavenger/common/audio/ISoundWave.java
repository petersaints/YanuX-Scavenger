/*
 * Copyright (c) 2020 Pedro Albuquerque Santos.
 *
 * This file is part of YanuX Scavenger.
 *
 * YanuX Scavenger is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * YanuX Scavenger is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with YanuX Scavenger. If not, see <https://www.gnu.org/licenses/gpl.html>
 */

package pt.unl.fct.di.novalincs.yanux.scavenger.common.audio;

public interface ISoundWave {
    int getDuration();

    void setDuration(int duration);

    int getSampleRate();

    void setSampleRate(int sampleRate);

    double getFrequency();

    void setFrequency(double frequency);

    int getSamples();

    void setSamples(int samples);

    //NOTE: For this to be truly generic, it should return byte[] instead of short[].
    //This way we are implying that the underlying data is a 16-bit buffer.
    short[] getData();

    int getByteSize();
}
