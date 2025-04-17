package Lop48K14_1.group2.brainnote.ui.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class DataProvider {

    private static List<Notebook> notebooks;

    static {
        initializeData();
    }

    private static void initializeData() {
        notebooks = new ArrayList<>();

        // Tạo dữ liệu mẫu
        List<Note> firstNotebookNotes = new ArrayList<>();
        firstNotebookNotes.add(new Note("101", "Làm việc", "Làm việc, kiểm tra thư cuối cùng\nThời gian: 12:30 21/01/2023", "21/01/2023"));
        firstNotebookNotes.add(new Note("102", "Tên ghi chú", "Nội dung ghi chú đầu\nThời gian: 14:15 22/01/2023", "22/01/2023"));
        firstNotebookNotes.add(new Note("103", "Tên ghi chú", "Nội dung ghi chú thứ hai\nThời gian: 09:45 23/01/2023", "23/01/2023"));

        List<Note> thirdNotebookNotes = new ArrayList<>();
        thirdNotebookNotes.add(new Note("301", "Làm việc", "Làm việc, kiểm tra thư cuối cùng\nThời gian: 12:30 21/01/2023", "21/01/2023"));

        notebooks.add(new Notebook("1", "First Notebook", firstNotebookNotes));
        notebooks.add(new Notebook("2", "Second Notebook", new ArrayList<>()));
        notebooks.add(new Notebook("3", "Third Notebook", thirdNotebookNotes));
        notebooks.add(new Notebook("4", "Fourth Notebook", new ArrayList<>()));
        notebooks.add(new Notebook("5", "Fifth Notebook", new ArrayList<>()));
        notebooks.add(new Notebook("6", "Sixth Notebook", new ArrayList<>()));
        notebooks.add(new Notebook("6", "Sixth Notebook", new ArrayList<>()));

        notebooks.add(new Notebook("7", "Sixth Notebook", new ArrayList<>()));

        notebooks.add(new Notebook("8", "Sixth Notebook", new ArrayList<>()));

        notebooks.add(new Notebook("9", "Sixth Notebook", new ArrayList<>()));

        notebooks.add(new Notebook("10", "Sixth Notebook", new ArrayList<>()));

        notebooks.add(new Notebook("11", "Sixth Notebook", new ArrayList<>()));


        notebooks.add(new Notebook("12", "Sixth Notebook", new ArrayList<>()));


    }

    public static List<Notebook> getNotebooks() {
        return notebooks;
    }

    public static Notebook getNotebookById(String id) {
        for (Notebook notebook : notebooks) {
            if (notebook.getId().equals(id)) {
                return notebook;
            }
        }
        return null;
    }

    public static void addNotebook(Notebook notebook) {
        notebooks.add(notebook);
    }

    public static void addNoteToNotebook(String notebookId, Note note) {
        Notebook notebook = getNotebookById(notebookId);
        if (notebook != null) {
            notebook.addNote(note);
        }
    }

    // Chuyển đổi dữ liệu sang JSON
    public static String getDataAsJson() {
        try {
            JSONArray notebooksArray = new JSONArray();

            for (Notebook notebook : notebooks) {
                JSONObject notebookObj = new JSONObject();
                notebookObj.put("id", notebook.getId());
                notebookObj.put("name", notebook.getName());

                JSONArray notesArray = new JSONArray();
                for (Note note : notebook.getNotes()) {
                    JSONObject noteObj = new JSONObject();
                    noteObj.put("id", note.getId());
                    noteObj.put("title", note.getTitle());
                    noteObj.put("content", note.getContent());
                    noteObj.put("date", note.getDate());
                    notesArray.put(noteObj);
                }

                notebookObj.put("notes", notesArray);
                notebooksArray.put(notebookObj);
            }

            JSONObject rootObj = new JSONObject();
            rootObj.put("notebooks", notebooksArray);

            return rootObj.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
