package Lop48K14_1.group2.brainnote.ui.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class DataProvider {

    private static List<Notebook> notebooks = new ArrayList<>();

    // Khởi tạo dữ liệu mẫu nếu cần
    public static void initializeSampleData() {

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

    // Cập nhật danh sách notebooks từ dữ liệu nhập
    public static void updateNotebooks(List<Notebook> newNotebooks) {
        notebooks.clear();
        notebooks.addAll(newNotebooks);
    }

    // Làm mới dữ liệu (xóa danh sách hiện tại)
    public static void clearData() {
        notebooks.clear();
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

    public static Note getNoteById(String notebookId, String noteId) {
        // Lấy sổ tay
        Notebook nb = getNotebookById(notebookId);
        if (nb == null) return null;

        // Duyệt qua các ghi chú
        for (Note n : nb.getNotes()) {
            if (n.getId().equals(noteId)) {
                return n;
            }
        }
        return null;
    }

    public static void removeNoteFromNotebook(String notebookId, String noteId) {
        Notebook nb = getNotebookById(notebookId);
        if (nb == null) return;

        // Dùng Iterator để tránh ConcurrentModificationException
        Iterator<Note> iter = nb.getNotes().iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(noteId)) {
                iter.remove();
                break;
            }
        }
    }
}
