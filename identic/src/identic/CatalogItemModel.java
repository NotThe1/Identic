package identic;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataListener;

public class CatalogItemModel extends DefaultListModel<CatalogItem>  {
	private static final long serialVersionUID = 1L;

	private List<CatalogItem> catalogItemList;

	public CatalogItemModel() {
		catalogItemList = new ArrayList<CatalogItem>();
	}//Constructor

	public void add(CatalogItem item) {
		catalogItemList.add(item);
	}// add

	public void add(int index, CatalogItem item) {
		catalogItemList.add(index, item);
	}// add

	public void clear() {
		catalogItemList.clear();
	}// clear

	public void removeElementAt(int index) {
		catalogItemList.remove(index);
	}// clear
	
	public CatalogItem get(int index) {
		return catalogItemList.get(index);
	}// get

	public CatalogItem getElementAt(int index) {
		return catalogItemList.get(index);
	}// getElementAt

	public int getSize() {
		return catalogItemList.size();
	}// getSize

	public boolean exists(String entryName) {
		boolean ans = false;
		for (CatalogItem ci : catalogItemList) {
			if (ci.entryName.equals(entryName)) {
				ans = true;
				break;
			} // if
		} // for
		return ans;
	}// exists

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}// addListDataListener

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}// removeListDataListener


}// class CatalogItemModel
