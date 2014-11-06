package edu.hitsz.nlp.struct;

public class PrefixTreeEnglish
{
	private TrieNode root;

    public PrefixTreeEnglish()
    {
    	root = new TrieNode('\0', false);
    }

    public void insertWord(String word)
    {
        int offset = 97;
        int l = word.length();
        char[] letters = word.toCharArray();
        TrieNode curNode = root;

        for (int i = 0; i < l; i++)
        {
            if (curNode.links[letters[i]-offset] == null)
                curNode.links[letters[i]-offset] = new TrieNode(letters[i], i == l-1 ? true : false);
            curNode = curNode.links[letters[i]-offset];
        }
    }

    private boolean find(String word)
    {
        char[] letters = word.toCharArray();
        int l = letters.length;
        int offset = 97;
        TrieNode curNode = root;

        int i;
        for (i = 0; i < l; i++)
        {
            if (curNode == null)
                return false;
            curNode = curNode.links[letters[i]-offset];
        }

        if (i == l && curNode == null)
            return false;

        if (curNode != null && !curNode.fullWord)
            return false;

        return true;
    }

    public void printTree(int level, char[] branch){
    	printTree(root, level, branch);
    }


    private void printTree(TrieNode root, int level, char[] branch)
    {
        if (root == null)
            return;

        for (int i = 0; i < root.links.length; i++)
        {
            branch[level] = root.letter;
            printTree(root.links[i], level+1, branch);
        }

        if (root.fullWord)
        {
            for (int j = 1; j <= level; j++)
                System.out.print(branch[j]);
            System.out.println();
        }
    }

    public static void main(String[] args)
    {
        PrefixTreeEnglish trie = new PrefixTreeEnglish();
        String[] words = {"an", "ant", "all", "allot", "alloy", "aloe", "are", "ate", "be"};
        for (int i = 0; i < words.length; i++)
            trie.insertWord(words[i]);

        char[] branch = new char[50];
        trie.printTree(0, branch);

        String searchWord = "all";
        if (trie.find(searchWord))
        {
            System.out.println("The word was found");
        }
        else
        {
            System.out.println("The word was NOT found");
        }
    }
}

class TrieNode
{
    char letter;
    TrieNode[] links;
    boolean fullWord;

    TrieNode(char letter, boolean fullWord)
    {
        this.letter = letter;
        links = new TrieNode[26];
        this.fullWord = fullWord;
    }
}
