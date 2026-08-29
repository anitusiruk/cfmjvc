package com.echoesofthepast.cultivation;

/**
 * Things a cultivator can work out or be taught. Discoveries never gate crafting - you can always
 * build a device - they gate <em>understanding</em>: an unlearned formation will not answer, a
 * reversed conversion wheel will not turn, a projection cannot be pushed out of the body.
 *
 * <p>Echo scrolls carry one of these each. So does successfully doing the thing in front of a
 * witness, which is why a scroll is a shortcut rather than a requirement.
 */
public final class Discovery {
    private Discovery() {}

    /** Qi can be seen at all. Granted the first time a device is read with a compass. */
    public static final String QI_SENSE = "qi_sense";
    /** Lines and nodes can be made to hold a circuit. */
    public static final String FORMATION_BASICS = "formation_basics";
    public static final String FORMATION_GATHERING = "formation_gathering";
    public static final String FORMATION_REPULSION = "formation_repulsion";
    public static final String FORMATION_CULTIVATION = "formation_cultivation";
    public static final String FORMATION_PRESERVATION = "formation_preservation";
    public static final String FORMATION_ATTUNEMENT = "formation_attunement";
    /** Turning the five phases backwards, against the generating cycle. */
    public static final String REVERSE_CYCLE = "reverse_cycle";
    /** Cutting facets that do more than split a beam. */
    public static final String PRISM_FACETS = "prism_facets";
    /** Teaching a tablet to repeat what it has watched. */
    public static final String ECHO_IMPRINTING = "echo_imprinting";
    /** Reading a cauldron well enough to make a perfect pill on purpose. */
    public static final String PILL_PERFECTION = "pill_perfection";
    /** Releasing a crescent of Qi from a blade. */
    public static final String SWORD_QI = "sword_qi";
    /** Stepping on air. */
    public static final String CLOUDSTEP = "cloudstep";
    /** Leaving the body. */
    public static final String NASCENT_PROJECTION = "nascent_projection";
    /** Choosing a spiritual root instead of accepting one. */
    public static final String ROOT_ATTUNEMENT = "root_attunement";
    /** Persuading tribulation lightning to strike something useful. */
    public static final String TRIBULATION_ROUTING = "tribulation_routing";
    /** Knowing what the shadow at a breakthrough actually is, and what quiets it. */
    public static final String HEART_DEMON_LORE = "heart_demon_lore";

    public static final String[] ALL = {
        QI_SENSE, FORMATION_BASICS, FORMATION_GATHERING, FORMATION_REPULSION, FORMATION_CULTIVATION,
        FORMATION_PRESERVATION, FORMATION_ATTUNEMENT, REVERSE_CYCLE, PRISM_FACETS, ECHO_IMPRINTING,
        PILL_PERFECTION, SWORD_QI, CLOUDSTEP, NASCENT_PROJECTION, ROOT_ATTUNEMENT,
        TRIBULATION_ROUTING, HEART_DEMON_LORE
    };

    public static String translationKey(String discovery) {
        return "eotp.discovery." + discovery;
    }
}
