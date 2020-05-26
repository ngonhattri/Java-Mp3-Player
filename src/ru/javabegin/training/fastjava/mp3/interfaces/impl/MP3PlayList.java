package ru.javabegin.training.fastjava.mp3.interfaces.impl;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import ru.javabegin.training.fastjava.mp3.gui.JListDropHandler;
import ru.javabegin.training.fastjava.mp3.interfaces.PlayList;
import ru.javabegin.training.fastjava.mp3.interfaces.Player;
import ru.javabegin.training.fastjava.mp3.objects.MP3;
import ru.javabegin.training.fastjava.mp3.utils.FileUtils;

// плейлист на основе компонента JList
public class MP3PlayList implements PlayList {

    public static final String PLAYLIST_FILE_EXTENSION = "pls";
    public static final String PLAYLIST_FILE_DESCRIPTION = "Файлы плейлиста";

    private static final String EMPTY_STRING = "";

    private Player player;

    private JList playlist;
    private DefaultListModel model = new DefaultListModel();

    public MP3PlayList(JList playlist, Player player) {
        this.playlist = playlist;
        this.player = player;
        initDragDrop();
        initPlayList();
    }

    @Override
    public void next() {
        int nextIndex = playlist.getSelectedIndex() + 1;
        if (nextIndex <= model.getSize() - 1) {// если не вышли за пределы плейлиста
            playlist.setSelectedIndex(nextIndex);
            playFile();
        }
    }

    @Override
    public void prev() {
        int nextIndex = playlist.getSelectedIndex() - 1;
        if (nextIndex >= 0) {// если не вышли за пределы плейлиста
            playlist.setSelectedIndex(nextIndex);
            playFile();
        }
    }

    @Override
    public boolean search(String name) {

        // если в поиске ничего не ввели - выйти из метода и не производить поиск
        if (name == null || name.trim().equals(EMPTY_STRING)) {
            return false;
        }

        // все индексы объектов, найденных по поиску, будут храниться в коллекции
        ArrayList<Integer> mp3FindedIndexes = new ArrayList<Integer>();

        // проходим по коллекции и ищем соответствия имен песен со строкой поиска
        for (int i = 0; i < model.getSize(); i++) {
            MP3 mp3 = (MP3) model.getElementAt(i);
            // поиск вхождения строки в название песни без учета регистра букв
            if (mp3.getName().toUpperCase().contains(name.toUpperCase())) {
                mp3FindedIndexes.add(i);// найденный индексы добавляем в коллекцию
            }
        }

        // коллекцию индексов сохраняем в массив
        int[] selectIndexes = new int[mp3FindedIndexes.size()];

        if (selectIndexes.length == 0) {// если не найдено ни одной песни, удовлетворяющей условию поиска
            return false;
        }
        // преобразовать коллекцию в массив, т.к. метод для выделения строк в JList работает только с массивом
        for (int i = 0; i < selectIndexes.length; i++) {
            selectIndexes[i] = mp3FindedIndexes.get(i).intValue();
        }

        // выделить в плелисте найдные песни по массиву индексов, найденных ранее
        playlist.setSelectedIndices(selectIndexes);

        return true;
    }

    @Override
    public boolean savePlaylist(File file) {
        try {
            String fileExtension = FileUtils.getFileExtension(file);

            // имя файла (нужно ли добавлять раширение к имени файлу при сохранении)
            String fileNameForSave = (fileExtension != null && fileExtension.equals(PLAYLIST_FILE_EXTENSION)) ? file.getPath() : file.getPath() + "." + PLAYLIST_FILE_EXTENSION;

            FileUtils.serialize(model, fileNameForSave);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean openFiles(File[] files) {

        boolean status = false;

        for (File file : files) {
            MP3 mp3 = new MP3(file.getName(), file.getPath());

            // если эта песня уже есть в списке - не добавлять ее
            if (!model.contains(mp3)) {
                model.addElement(mp3);
                status = true;
            }
        }

        return status;
    }

    @Override
    public void playFile() {
        int[] indexPlayList = playlist.getSelectedIndices();// получаем выбранные индексы(порядковый номер) песен
        if (indexPlayList.length > 0) {// если выбрали хотя бы одну песню
            Object selectedItem = model.getElementAt(indexPlayList[0]);
            if (!(selectedItem instanceof MP3)) {
                return;
            }
            MP3 mp3 = (MP3) selectedItem;// находим первую выбранную песню (т.к. несколько песен нельзя проиграть одновременно
            player.play(mp3.getPath());
        }

    }

    @Override
    public boolean openPlayList(File file) {
        try {
            DefaultListModel mp3ListModel = (DefaultListModel) FileUtils.deserialize(file.getPath());
            this.model = mp3ListModel;
            playlist.setModel(mp3ListModel);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void delete() {
        int[] indexPlayList = playlist.getSelectedIndices();// получаем выбранные индексы(порядковый номер) песен

        if (indexPlayList.length > 0) {// если выбрали хотя бы одну песню

            ArrayList<MP3> mp3ListForRemove = new ArrayList<MP3>();// сначала сохраняем все mp3 для удаления в отдельную коллекцию

            for (int i = 0; i < indexPlayList.length; i++) {// удаляем все выбранные песни из плейлиста
                MP3 mp3 = (MP3) model.getElementAt(indexPlayList[i]);
                mp3ListForRemove.add(mp3);
            }

            // удаляем mp3 в плейлисте
            for (MP3 mp3 : mp3ListForRemove) {
                model.removeElement(mp3);
            }

        }
    }

    @Override
    public void clear() {
        model.clear();
    }

    private void initPlayList() {

        playlist.setModel(model);
        playlist.setToolTipText("Список песен");

        playlist.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // если нажали левую кнопку мыши 2 раза
                if (evt.getModifiers() == InputEvent.BUTTON1_MASK && evt.getClickCount() == 2) {
                    playFile();
                }
            }
        });

        playlist.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                int key = evt.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    playFile();
                }
            }
        });
    }

    private DropTarget dropTarget;

    private void initDragDrop() {

        try {
            dropTarget = new DropTarget(playlist, DnDConstants.ACTION_COPY_OR_MOVE, null);
            dropTarget.addDropTargetListener(new JListDropHandler(playlist));

        } catch (TooManyListenersException ex) {
            Logger.getLogger(MP3PlayList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
