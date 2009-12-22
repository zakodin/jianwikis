package org.telluriumsource;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;


/**
 * @author Jian Fang (John.Jian.Fang@gmail.com)
 *
 * Date: Dec 21, 2009
 */
public class Trie_UT {

    @Test
    public void testInsert(){
        String[] dictionary = {"a", "an", "and", "andy", "bo", "body", "bodyguard", "some", "someday", "goodluck", "joke"};
        Trie trie = new Trie();
        trie.buildTree(dictionary);
        trie.checkAndIndexLevel();
        trie.printMe();
        Node deepest = trie.getDeepestNode();
        assertNotNull(deepest);
        System.out.println("The word with longest prefix chain: " + deepest.getFullWord() + ", level: " + deepest.getLevel());

    }

}
