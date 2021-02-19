package wafflestomper.ghostwriter;

import net.minecraft.client.gui.screen.LecternScreen;
import net.minecraft.client.gui.screen.ReadBookScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GhostwriterLecternScreen extends LecternScreen implements IGhostBook{

	private final GhostLayer ghostLayer;
	private final LecternContainer lecternContainer;


	public GhostwriterLecternScreen(ItemStack currStack, LecternContainer lecternContainer, PlayerInventory playerInventory) {
		// Not sure why it needs the inventory and text. Both params are ignored by the constructor
		super(lecternContainer, playerInventory, new StringTextComponent(""));
		
		this.ghostLayer = new GhostLayer(this, this, false);
		this.lecternContainer = lecternContainer;
		if (currStack != null){
			CompoundNBT compoundnbt = currStack.getTag();
			if (compoundnbt != null) {
				this.ghostLayer.setTitleAuthor(compoundnbt.getString("title"), compoundnbt.getString("author"));
			}
		}
	}


	/**
	 * Helper function that extracts the pages from the read book until I find a cleaner way to do this
	 * @return Pages as a list of Strings
	 */
	// TODO: We can probably drop this in favor of pagesAsList, which should use the IBookInfo methods to get the page text
	public List<String> extractBookPages(){
		if (this.bookInfo instanceof ReadBookScreen.WrittenBookInfo) {
			ReadBookScreen.WrittenBookInfo b = (ReadBookScreen.WrittenBookInfo)this.bookInfo;
			return b.pages;
		}
		else if (this.bookInfo instanceof ReadBookScreen.UnwrittenBookInfo) {
			ReadBookScreen.UnwrittenBookInfo b = (ReadBookScreen.UnwrittenBookInfo)this.bookInfo;
			return b.pages;
		}
		else {
			return new ArrayList<>();
		}
	}
	
	
	/**
	 * Override from ReadBookScreen
	 */
	@Override  // From ReadBookScreen
	public void updateButtons() {
		this.ghostLayer.updateButtons();
	}
	
	
	@Override  // From IGhostBook
	public List<String> pagesAsList(){
		// TODO: Perhaps we should use the getPage() method provided by IBookInfo instead? I think that would negate the
		//       JSON weirdness we get from some read books
		List<String> pages = new ArrayList<>();
		for (int i=0; i<this.getPageCount(); i++){
			// Ugly hack to convert the new JSON "Yo dawg I heard you like strings, so I put a string in your string" strings
			//  back to the old-style literal strings that everyone knows and loves. I'll update this to do the opposite once
			//  we're finally allowed to send JSON strings to the server. It also converts to old-school formatting codes
			String pageText = BookUtilities.deJSONify(this.extractBookPages().get(i)); // TODO: Should this use the getPage function from IBookInfo instead?
			pages.add(pageText);
		}
		return pages;
	}
	
	
	@Override  // From LecternScreen
	public void init() {
		super.init();
		this.ghostLayer.init();
		this.updateButtons();
		// This is a hack based on LecternScreen.func_214176_h()
		// Books can be left open to a specific page on a lectern. This displays that page.
		// Otherwise we'd just be showing the first page every time
		this.showPage(this.lecternContainer.getPage());
	}
	
	
	@Override  // From IGhostBook
	public int getBookPageCount(){
		return this.getPageCount();
	}
	
	
	@Override  // From IGhostBook
	public void updateVanillaButtons(){
		super.updateButtons();
	}
	

	@Override  // From IGhostBook
	public String getPageText(int pageNum) {
		return this.bookInfo.func_238806_b_(pageNum).getString();
	}

	
	@Override  // From IGhostBook
	public boolean isBookBeingSigned() {
		return false;
	}
	
	
	@Override  // From IGhostBook
	public void bookChanged(boolean setModifiedFlag) {
		this.cachedPage = -1;
	}
	
	
	@Override  // From IGhostBook
	public Button addGhostButton(Button button) {
		return this.addButton(button);
	}
	
	
	@Override  // From IGhostBook
	public int getCurrPage() {
		return this.currPage;
	}
	
	
	@Override  // From IGhostBook
	public String getBookTitle() {
		return this.ghostLayer.bookTitle;
	}
	
	
	// Unused methods that only apply to unsigned books
	@Override  // From IGhostBook
	public void setCurrPage(int pageNum){}
	@Override  // From IGhostBook
	public void setBookTitle(String title){}
	@Override  // From IGhostBook
	public void setPageText(int pageNum, String pageText){}
	@Override  // From IGhostBook
	public void insertText(String insertChars){}
	@Override  // From IGhostBook
	public void insertNewPage(int atPageNum, String pageText){}
	@Override  // From IGhostBook
	public void removePage(int pageNum){}
	@Override  // From IGhostBook
	public void replaceBookPages(List<String> newPages){}
}
