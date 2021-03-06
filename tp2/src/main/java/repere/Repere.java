package repere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import repere.formes.Cercle;
import repere.formes.Point;
import repere.formes.Segment;
import repere.formes.Triangle;

public class Repere {

	private static final String REGEX_COULEUR = "\\[(\\d+),(\\d+),(\\d+)\\]";		
	private static final Pattern PATTERN_REPERE = Pattern.compile("Repere (.*)");
	private static final Pattern PATTERN_AXE = Pattern.compile(String.format("Axe (\\d+) %s (.*)", REGEX_COULEUR));
	private static final Pattern PATTERN_POINT = Pattern.compile(String.format("Point \\((\\d+),(\\d+)\\) %s", REGEX_COULEUR));
	private static final Pattern PATTERN_SEGMENT = Pattern.compile(String.format("Segment %s %s %s", REGEX_COULEUR, PATTERN_POINT, PATTERN_POINT));
	private static final Pattern PATTERN_CERCLE = Pattern.compile(String.format("Cercle (\\d+) %s %s", REGEX_COULEUR, PATTERN_POINT));
	private static final Pattern PATTERN_TRIANGLE = Pattern.compile(String.format("Triangle %s %s %s %s", REGEX_COULEUR, PATTERN_POINT, PATTERN_POINT, PATTERN_POINT));

	private String titre;
	
	private Set<ElementRepere> elements;

	private Axe x;

	private Axe y;

	public Repere(String titre, Axe x, Axe y) {
		this.titre = titre;
		this.x = x;
		this.y = y;
		this.elements = new HashSet<ElementRepere>();
	}

	public static Repere charger(Reader reader) throws IOException, HorsRepereException {
		BufferedReader r = new BufferedReader(reader);
		Repere repere = chargeRepere(r);
		chargeElements(repere, r);
		return repere;
	}

	private static Repere chargeRepere(BufferedReader r) throws IOException {
		Matcher repereDef = PATTERN_REPERE.matcher(r.readLine());
		Matcher xDef = PATTERN_AXE.matcher(r.readLine());
		Matcher yDef = PATTERN_AXE.matcher(r.readLine());
		
		repereDef.matches();
		String titre = repereDef.group(1);
		xDef.matches();
		Axe x = new Axe(couleurPourMatcher(xDef, 2), xDef.group(5), Integer.parseInt(xDef.group(1)));
		yDef.matches();
		Axe y = new Axe(couleurPourMatcher(yDef, 2), yDef.group(5), Integer.parseInt(yDef.group(1)));
		return new Repere(titre, x, y);
	}

	private static void chargeElements(Repere repere, BufferedReader r) throws IOException, HorsRepereException {
		String line;
		while ((line = r.readLine()) != null) {
			ElementRepere element = null;
			switch (line.charAt(0)) {
				case 'P':
					Matcher pDef = PATTERN_POINT.matcher(line);
					pDef.matches();
					element = pointPourMatcher(pDef, 1);
					break;
				case 'S':
					Matcher sDef = PATTERN_SEGMENT.matcher(line);
					sDef.matches();
					element = new Segment(couleurPourMatcher(sDef, 1), pointPourMatcher(sDef, 4),
							pointPourMatcher(sDef, 9));
					break;
				case 'C':
					Matcher cDef = PATTERN_CERCLE.matcher(line);
					cDef.matches();
					element = new Cercle(couleurPourMatcher(cDef, 2), pointPourMatcher(cDef, 5), 
							Integer.parseInt(cDef.group(1)));
					break;
				case 'T':
					Matcher tDef = PATTERN_TRIANGLE.matcher(line);
					tDef.matches();
					element = new Triangle(couleurPourMatcher(tDef, 1), pointPourMatcher(tDef, 4),
							pointPourMatcher(tDef, 9), pointPourMatcher(tDef, 14));
					break;
			}
			if (element != null)
				repere.ajouter(element);
		}
	}
	
	private static Couleur couleurPourMatcher(Matcher def, int offset) {
		return new Couleur(Integer.parseInt(def.group(offset)), Integer.parseInt(def.group(offset + 1)), 
				Integer.parseInt(def.group(offset + 2)));
	}
	
	private static Point pointPourMatcher(Matcher def, int offset) {
		return new Point(couleurPourMatcher(def, offset + 2), Integer.parseInt(def.group(offset)), 
				Integer.parseInt(def.group(offset + 1)));
	}
	
	public Axe getX() {
		return x;
	}

	public Axe getY() {
		return y;
	}
	
	public String getTitre() {
		return this.titre;
	}

	public void ajouter(ElementRepere e) throws HorsRepereException {
		if (e.validePour(this))
			elements.add(e);
		else
			throw new HorsRepereException();
	}
	
	public Set<ElementRepere> getElements() {
		return elements;
	}

	public void sauvegarder(Writer w) throws IOException {
		w.append(String.format("Repere %s\n", titre));
		w.append(String.format("%s\n", x.serialisation()));
		w.append(String.format("%s\n", y.serialisation()));
		for (ElementRepere e: elements)
			w.append(String.format("%s\n", e.serialisation()));
		w.close();
	}
	
	public void dessiner(Writer w) throws IOException {
		w.append("<?xml version='1.0' encoding='utf-8'?>\n");
		w.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>\n", x.getTaille() * 10, y.getTaille() * 10));
		w.append(String.format("<line x1='0' y1='0' x2='%d' y2='0' style='stroke:%s;stroke-width:3' />\n", x.getTaille() * 10, x.getCouleur().svg()));
		w.append(String.format("<line x1='0' y1='0' y2='%d' x2='0' style='stroke:%s;stroke-width:3' />\n", y.getTaille() * 10, y.getCouleur().svg()));
		for( ElementRepere e: elements )
			w.append(String.format("%s\n", e.svg()));
		w.append("</svg>");
		w.close();
	}

}
