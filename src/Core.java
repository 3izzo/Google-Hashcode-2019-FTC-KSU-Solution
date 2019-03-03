import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Core {
    public int count;

    public List<Picture> pictures = new ArrayList<Picture>();
    public List<Slide> slidesInput = new ArrayList<Slide>();
    public List<Slide> slides = new ArrayList<Slide>();
    public HashMap<String, LinkedList<Slide>> tagMap = new HashMap<String, LinkedList<Slide>>();
    public long score = 0;

    private int emptyTill = 0;
    static int maxIteration = 100;
//    long seed;
//    Random r = new Random();

    public static void main(String[] args) throws FileNotFoundException {

	File folder = new File("inputs");
	for (final File fileEntry : folder.listFiles()) {
//	for (int i = 0; i < 4; i++) {
	    new Thread() {
		@Override
		public void run() {
		    try {
//			while (true)
			    new Core(fileEntry.getName());
		    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }.start();
//	    System.out.println("created threead: " + i);
	}
//	    break;
//	}
    }

    public Core(String path) throws FileNotFoundException {
//	seed = System.nanoTime();
//	r = new Random(seed);
	Scanner sc = new Scanner(new File("inputs\\" + path));
	count = sc.nextInt();

	for (int i = 0; i < count; i++) {
	    boolean v = sc.next().equals("V");
	    int n = sc.nextInt();
	    Set<String> tags = new HashSet<String>();

	    for (int j = 0; j < n; j++) {
		tags.add(sc.next());
	    }
	    pictures.add(new Picture(i, v, tags));
	}
	try {
	    Solve("out_" + path);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void Solve(String outPut) throws IOException {
	getCombinedSlides();
	getHorizontalSlides();
//	System.out.println("horizantal slides " + outPut);
	fillTagMap();
//	System.out.println("filled map " + outPut);

	calcTagRep();
//	System.out.println("calculated tags " + outPut);
	slides = new LinkedList<Slide>(slidesInput);
	slides.sort(new Comparator<Slide>() {

	    @Override
	    public int compare(Slide o1, Slide o2) {
		return o1.tagRep - o2.tagRep;
	    }
	});
//	System.out.println("sorted  " + outPut);

	Slide prevSlide;
	Slide currentSlide = findBestPair(slides.get(0));

	FileWriter fileWriter = new FileWriter(outPut);
	PrintWriter printWriter = new PrintWriter(fileWriter);
	printWriter.printf("%d\n", slides.size());
	for (int i = 0; i < slides.size(); i++) {
//	    if (i % 1000 == 0) {
//		System.out.println(i);
//	    }
//	    System.out.println(slides.indexOf(currentSlide));
	    printWriter.print("" + slides.indexOf(currentSlide));

	    if (currentSlide.isCombinedSlide())
		printWriter.printf("%d %d\n", currentSlide.p1.index, currentSlide.p2.index);
	    else
		printWriter.printf("%d\n", currentSlide.p1.index);

	    removeFromTagMap(currentSlide);
	    prevSlide = currentSlide;
	    currentSlide = findBestPair(currentSlide);
	    if (currentSlide != null)
		score += prevSlide.getTransitionValue(currentSlide);

	}
	System.out.println(outPut + " score: " + score);

	fileWriter.close();

//	System.out.println(tagMap);
    }

    private Slide findBestPair(Slide slide) {
	Slide bestPair = null;
	int bestValue = -1;
	int n = 0;
	main: for (String tag : slide.getTags()) {
//	    System.out.println(tag);

	    LinkedList<Slide> shared = tagMap.get(tag);
	    if (shared == null)
		continue;
	    for (Slide slide2 : shared) {
		if (slide2 == slide)
		    continue;
		if (maxIteration < n) {
//		    System.out.println("iteration");
		    break main;
		}
		n++;
		int transitionValue = slide.getTransitionValue(slide2);
		if (transitionValue > bestValue) {
		    bestPair = slide2;
		    bestValue = transitionValue;
		}
	    }
	}
	if (bestPair == null) {
//	    System.out.println(tagMap.size());
//	    System.out.println("no pair");
	    ArrayList<String> arrayList = new ArrayList<String>(tagMap.keySet());
	    for (int i = emptyTill; i < arrayList.size(); i++) {
		String tag = arrayList.get(i);
		try {
		    bestPair = tagMap.get(tag).get(0);
		    break;
		} catch (Exception e) {
		    emptyTill = i;
		}
	    }
	}
	return bestPair;
    }

    private void removeFromTagMap(Slide slide) {

	for (String tag : slide.getTags()) {
	    tagMap.get(tag).remove(slide);
	    if (tagMap.get(tag).size() == 0)
		tagMap.remove(tag);
	}
    }

    private void calcTagRep() {
	for (int i = 0; i < slides.size(); i++) {
	    Slide slide = slides.get(i);
	    for (String tag : slide.getTags()) {
		int size = tagMap.get(tag).size();

		slide.tagRep += size;
	    }
	}
    }

    private void getHorizontalSlides() {
	for (Picture picture : pictures) {
	    if (!picture.vertical) {
		slidesInput.add(new Slide(picture, null));
	    }
	}
    }

    private void fillTagMap() {
	for (Slide slide : slidesInput) {
	    for (String tag : slide.getTags()) {
		if (tagMap.get(tag) == null) {
		    LinkedList<Slide> linkedList = new LinkedList<>();
		    tagMap.put(tag, linkedList);
		}
		tagMap.get(tag).add(slide);
	    }
	}
    }

    private void getCombinedSlides() {
	LinkedList<Picture> hPics = new LinkedList<>();
	getVerticalList(hPics);

	hPics.sort(new Comparator<Picture>() {

	    @Override
	    public int compare(Picture o1, Picture o2) {
		return o1.tags.size() - o2.tags.size();
	    }
	});

//	HashMap<String, LinkedList<Picture>> h = new HashMap<>();
//	HashMap<String, LinkedList<Picture>> l = new HashMap<>();
	for (int i = 0; i < hPics.size() / 2; i++) {
//	    System.out.println(i);
	    slidesInput.add(new Slide(hPics.get(i), hPics.get(hPics.size() - i - 1)));
//	    Picture high = hPics.get(hPics.size() - 1 - i);
//	    Picture low = hPics.get(i);
//
//	    for (String tag : high.tags) {
//		if (h.get(tag) == null) {
//		    LinkedList<Picture> linkedList = new LinkedList<>();
//		    h.put(tag, linkedList);
//		}
//		h.get(tag).add(high);
//	    }
//	    for (String tag : low.tags) {
//		if (h.get(tag) == null) {
//		    LinkedList<Picture> linkedList = new LinkedList<>();
//		    h.put(tag, linkedList);
//		}
//		h.get(tag).add(low);
//	    }

	}
//	for (int i = 0; i < hPics.size() / 2; i++) {
//	    Picture low = hPics.get(i);
//
//	    Picture lestCommonTags = hPics.get(hPics.size() - 1 - i);
//	    int max = 0;
//	    String maxKey = "";
//	    for (String string : low.tags) {
//		if (h.get(string).size() > max) {
//		    max = h.get(string).size();
//		    maxKey = string;
//		}
//	    }
//	    Slide s = new Slide(h.get(maxKey).get(0), low);
//	    slidesInput.add(s);
//	}

    }

    private void getVerticalList(LinkedList<Picture> hPics) {
	for (int i = 0; i < pictures.size(); i++) {
	    Picture picture = pictures.get(i);
	    if (picture.vertical) {
		hPics.add(picture);
	    }
	}
    }
}

class Picture {
    int index;
    boolean vertical;
    Set<String> tags;

    public Picture(int index, boolean vertical, Set<String> tags) {
	this.vertical = vertical;
	this.tags = tags;
	this.index = index;
    }
}

class Slide {
    Picture p1, p2;
    private Set<String> allTags;
    int tagRep = 0;

    public Slide(Picture p1, Picture p2) {
	this.p1 = p1;
	this.p2 = p2;

	if (isCombinedSlide()) {
	    allTags = new HashSet<>(p1.tags);
	    allTags.addAll(p2.tags);
	} else
	    allTags = p1.tags;
    }

    public boolean isCombinedSlide() {
	return p2 != null;
    }

    public Set<String> getTags() {
	return allTags;
    }

    public int getTransitionValue(Slide s) {
	int commonTags = 0;
	for (String string : allTags) {
	    if (s.allTags.contains(string))
		commonTags++;
	}
	int unq1 = allTags.size() - commonTags, un2 = s.allTags.size() - commonTags;
	return Math.min(Math.min(commonTags, unq1), un2);
    }

    @Override
    public String toString() {
	return "Slide : tags:" + allTags.toString() + " tagRep: " + tagRep;
    }
}
